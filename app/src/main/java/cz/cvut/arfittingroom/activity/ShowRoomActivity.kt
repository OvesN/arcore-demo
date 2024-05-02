package cz.cvut.arfittingroom.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.filament.LightManager
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFrontFacingFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StreamDownloadTask
import com.gorisse.thomas.sceneform.environment
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.light.build
import com.gorisse.thomas.sceneform.lightEstimationConfig
import com.gorisse.thomas.sceneform.mainLight
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.databinding.ActivityShowRoomBinding
import cz.cvut.arfittingroom.fragment.AccessoriesOptionsFragment
import cz.cvut.arfittingroom.fragment.ColorOptionsFragment
import cz.cvut.arfittingroom.fragment.LooksOptionsFragment
import cz.cvut.arfittingroom.fragment.MakeupOptionsFragment
import cz.cvut.arfittingroom.model.MakeupInfo
import cz.cvut.arfittingroom.model.ModelInfo
import cz.cvut.arfittingroom.model.enums.ENodeType
import cz.cvut.arfittingroom.service.MakeupService
import cz.cvut.arfittingroom.service.ModelEditorService
import cz.cvut.arfittingroom.utils.BitmapUtil.replaceNonTransparentPixels
import cz.cvut.arfittingroom.utils.FileUtil.doesTempAnimatedMaskExist
import cz.cvut.arfittingroom.utils.FileUtil.getNextTempMaskFrame
import cz.cvut.arfittingroom.utils.FileUtil.getTempMaskTextureBitmap
import java.util.concurrent.Callable
import javax.inject.Inject

class ShowRoomActivity : AppCompatActivity(), ResourceListener {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }

    private val binding: ActivityShowRoomBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var arFragment: ArFrontFacingFragment
    private lateinit var arSceneView: ArSceneView
    private lateinit var storage: FirebaseStorage

    private var faceNodeMap = HashMap<AugmentedFace, HashMap<ENodeType, AugmentedFaceNode>>()
    private var isUpdated = false

    private var shouldPlayAnimation = false
    private var gifPrepared = false

    private val gifTextures = mutableListOf<Texture>()

    private var frameCounter = 0

    @Inject
    lateinit var makeUpService: MakeupService

    @Inject
    lateinit var editorService: ModelEditorService

    private var handler = Handler(Looper.getMainLooper())
    private var gifRunnable: Runnable? = null
    private var frameDelay: Long = 100 // Default frame delay (100 ms per frame)

    private val accessoriesOptionsFragment = AccessoriesOptionsFragment()
    private val looksOptionsFragment = LooksOptionsFragment()
    private val makeupOptionsFragment = MakeupOptionsFragment()
    private val colorOptionFragment = ColorOptionsFragment()

    override fun onResume() {
        super.onResume()
        resetGifState()

        val bitmap = getTempMaskTextureBitmap(applicationContext)
        bitmap?.let { createTextureAndApply(it); shouldPlayAnimation = false }

        shouldPlayAnimation = doesTempAnimatedMaskExist(applicationContext)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }

        storage = FirebaseStorage.getInstance()
        setContentView(binding.root)

        supportFragmentManager.addFragmentOnAttachListener { fragmentManager: FragmentManager, fragment: Fragment ->
            this.onAttachFragment(
                fragmentManager,
                fragment
            )
        }

        if (savedInstanceState == null) {
            addMenuFragments()
            showMakeupOptionsMenu()

            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFrontFacingFragment::class.java, null)
                    .commit()
            }
        }

        // Set click listeners
        binding.makeupButton.setOnClickListener {
            showMakeupOptionsMenu()
        }
        binding.accessoriesButton.setOnClickListener {
            showAccessoriesMenu()
        }
        binding.looksButton.setOnClickListener {
            showLooksMenu()
        }

//        setupButtonClickListener(binding.buttonLiner, EMakeupType.LINER)
//        setupButtonClickListener(binding.buttonBlush, EMakeupType.BLUSH)
//        setupButtonClickListener(binding.buttonLipstick, EMakeupType.LIPSTICK)
//        setupButtonClickListener(
//            binding.buttonYellowSunglasses,
//            EAccessoryType.YELLOW_GLASSES,
//            EModelType.EYES
//        )
//        setupButtonClickListener(
//            binding.buttonSunglasses,
//            EAccessoryType.SUNGLASSES,
//            EModelType.EYES
//        )
//        setupButtonClickListener(
//            binding.buttonMarioHat,
//            EAccessoryType.MARIO_HAT,
//            EModelType.TOP_HEAD
//        )
//
//        binding.button3dEditor.setOnClickListener {
//            editorButtonListener()
//        }

        binding.buttonMaskEditor.setOnClickListener {
            val intent = Intent(this, MakeupEditorActivity::class.java)
            startActivity(intent)
        }
    }


    override fun applyImage(type: String, ref: String, color: Int) {
        makeUpService.appliedMakeUpTypes[type] = MakeupInfo(ref, color)

        makeUpService.appliedMakeUpTypes.values.forEach {
            loadImage(it.ref, it.color)
        }
    }

    override fun removeImage(type: String) {
        val appliedMakeupTypes = makeUpService.appliedMakeUpTypes
        appliedMakeupTypes.remove(type)

        // Clear faceNode with makeup
        if (appliedMakeupTypes.isEmpty()) {
            faceNodeMap.values
                .mapNotNull { map -> map[ENodeType.MAKEUP] }
                .forEach { node -> node.faceMeshTexture = null }
        } else {
            makeUpService.appliedMakeUpTypes.values.forEach {
                loadImage(it.ref, it.color)
            }
        }
    }


    override fun applyModel(modelInfo: ModelInfo) {
        makeUpService.loadedModels[modelInfo.nodeType] = modelInfo
        loadModel(modelInfo.modelRef, modelInfo.nodeType)
    }

    override fun removeModel(type: String) {
        //toggleModelOnFaceNodes(model)
    }


    private fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFrontFacingFragment
            arFragment.setOnViewCreatedListener { arSceneView: ArSceneView ->
                onViewCreated(
                    arSceneView
                )
            }
        }
    }

    private fun onViewCreated(arSceneView: ArSceneView) {
        this.arSceneView = arSceneView
        arSceneView.lightEstimationConfig = LightEstimationConfig.DISABLED
        val light = LightManager.Builder(LightManager.Type.POINT)
            .position(-0.4f, 0.0f, -0.2f)
            .intensity(200000.0f)
            .color(0.98f, 0.89f, 0.76f)
            .sunAngularRadius(1.9f)
            .sunHaloSize(10.0f)
            .sunHaloFalloff(80.0f)
            .castShadows(true).build()
        arSceneView.environment?.indirectLight?.intensity = 5000f
        arSceneView.mainLight = light

        // This is important to make sure that the camera stream renders first so that
        // the face mesh occlusion works correctly.
        arSceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST)

        // Check for face detections
        arFragment.setOnAugmentedFaceUpdateListener {
            this.onAugmentedFaceTrackingUpdate(
            )
        }
    }

    private fun onAugmentedFaceTrackingUpdate() {
        // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking
        faceNodeMap.entries.removeIf { (face, nodes) ->
            if (face.trackingState == TrackingState.STOPPED) {
                nodes.forEach { entry -> entry.value.parent = null }
                true
            } else {
                false
            }
        }
        if (shouldPlayAnimation) {
            if (!gifPrepared) {
                prepareAllGifTextures()
            } else if (gifRunnable == null) {
                startAnimation()
            }

        } else if (!isUpdated) {
            updateModelsOnScreen()
        }
    }


    private fun applyModel(renderable: ModelRenderable, nodeType: ENodeType) {
        val sceneView = arFragment.arSceneView
        // Update nodes
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            modelNodesMap[nodeType] = AugmentedFaceNode(face).also { node ->
                node.parent = sceneView.scene
            }.apply { faceRegionsRenderable = renderable }

        }
    }

    private fun updateModelsOnScreen() {
        val sceneView = arFragment.arSceneView
        val modelsToUpdate = editorService.loadedModels
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            isUpdated = true
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            modelsToUpdate.forEach { entry ->
                val faceNode = AugmentedFaceNode(face).also { node ->
                    node.parent = sceneView.scene

                }
                modelNodesMap[entry.value.nodeType] = faceNode
//                faceNode.faceRegionsRenderable = entry.value.model
//                entry.value.isActive = true

                //makeUpService.loadedModels[entry.key] = entry.value
            }
            makeUpService.textureBitmap?.let {
                createTextureAndApply(it)
            }

            editorService.loadedModels.clear()

        }
    }

    private fun prepareAllGifTextures() {
        gifTextures.clear()
        var counter = 0

        while (true) {
            val frameBitmap = getNextTempMaskFrame(applicationContext, counter) ?: break
            Texture.builder()
                .setSource(frameBitmap)
                .build()
                .thenAccept { texture ->
                    gifTextures.add(texture)
                }
                .exceptionally { throwable ->
                    Log.println(
                        Log.ERROR,
                        null,
                        "Error creating texture for frame $counter: $throwable"
                    )
                    null
                }
            counter++
        }

        gifPrepared = true
    }

    private fun createTextureAndApply(combinedBitmap: Bitmap) {
        makeUpService.textureBitmap = combinedBitmap

        // Convert Bitmap to ARCore Texture
        Texture.builder()
            .setSource(combinedBitmap)
            .build()
            .thenAccept { texture -> applyTextureToAllFaces(texture) }
            .exceptionally {
                Log.println(Log.ERROR, null, "Error during texture initialisation")
                null
            }
    }

    // Apply a Bitmap texture to all detected faces
    private fun applyTextureToAllFaces(texture: Texture) {
        val sceneView = arFragment.arSceneView

        // Update nodes
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            // Check if the node for the makeup already exist
            if (modelNodesMap[ENodeType.MAKEUP] == null) {
                val faceNode = AugmentedFaceNode(face).also { node ->
                    node.parent = sceneView.scene
                }
                modelNodesMap[ENodeType.MAKEUP] = faceNode
                faceNode.faceMeshTexture = texture
            } else {
                modelNodesMap[ENodeType.MAKEUP]?.faceMeshTexture = texture
            }
        }
    }

    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        if (ArCoreApk.getInstance()
                .checkAvailability(this) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE
        ) {
            Toast.makeText(this, "Augmented Faces requires ARCore", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val openGlVersionString = (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.deviceConfigurationInfo
            ?.glEsVersion

        openGlVersionString?.let {
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
                finish()
                return false
            }
        }
        return true
    }

    private fun loadModel(ref: String, modelType: ENodeType) {

        storage.getReference(ref).downloadUrl.addOnSuccessListener {
            ModelRenderable.builder()
                .setSource(this, it)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { model ->
                    applyModel(model, modelType)
                }
                .exceptionally { ex ->
                    Log.println(Log.ERROR, null, ex.message.orEmpty())
                    null
                }
        }
            .addOnFailureListener { ex ->
                Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
                Log.println(Log.ERROR, null, ex.message.orEmpty())
            }


//        storage
//            .getReference(ref)
//            .getStream(StreamDownloadTask.StreamProcessor())
//            .addOnSuccessListener {
//                ModelRenderable.builder()
//                    .setSource(this) { it.stream }
//                    .setIsFilamentGltf(true)
//                    .build()
//                    .thenAccept { model ->
//                        applyModel(model, modelType)
//                    }
//                    .exceptionally { ex ->
//                        Log.println(Log.ERROR, null, ex.message.orEmpty())
//                        null
//                    }
//            }
//            .addOnFailureListener { ex ->
//                Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
//                Log.println(Log.ERROR, null, ex.message.orEmpty())
//            }


    }

    private fun loadImage(ref: String, color: Int) {
        Glide.with(this)
            .asBitmap()
            .load(storage.getReference(ref))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    replaceNonTransparentPixels(resource, color)
                    makeUpService.makeUpBitmaps.add(resource)

                    if (makeUpService.areMakeupBitmapsPrepared()) {
                        val combinedBitmap = makeUpService.combineBitmaps()
                        combinedBitmap?.let { createTextureAndApply(it) }
                    }
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    private fun toggleModelOnFaceNodes(modelInfo: ModelInfo) {
        val sceneView = arFragment.arSceneView

        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            val faceNode = modelNodesMap.getOrPut(modelInfo.nodeType) {
                AugmentedFaceNode(face).also { node ->
                    node.parent = sceneView.scene
                }
            }
//            if (faceNode.faceRegionsRenderable == modelInfo.model && modelInfo.isActive) {
//                // If the model is currently applied, remove it
//                faceNode.isEnabled = false
//                modelInfo.isActive = false
//            } else {
//                // If the model is not applied, apply it
//                faceNode.isEnabled = true
//                faceNode.faceRegionsRenderable = modelInfo.model
//                modelInfo.isActive = true
//            }
        }
    }

    private fun startAnimation() {
        if (!gifPrepared) {
            return
        }

        if (gifRunnable == null) {
            gifRunnable = Runnable {
                if (!gifPrepared) {
                    prepareAllGifTextures()
                } else {
                    if (gifTextures.size > frameCounter) {
                        applyTextureToAllFaces(gifTextures[frameCounter])
                        frameCounter = (frameCounter + 1) % gifTextures.size
                    }
                    handler.postDelayed(gifRunnable!!, frameDelay)
                }

            }
        }
        gifRunnable?.let { handler.post(it) }
    }

    private fun stopAnimation() {
        gifRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun setSpeed(fps: Int) {
        frameDelay = 1000L / fps
    }

    private fun resetGifState() {
        gifPrepared = false
        gifRunnable = null
    }

    private fun addMenuFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.menu_fragment_container, makeupOptionsFragment)
            .add(R.id.menu_fragment_container, colorOptionFragment)
            .add(R.id.menu_fragment_container, accessoriesOptionsFragment)
            .add(R.id.menu_fragment_container, looksOptionsFragment)
            .commit()
    }

    private fun showMakeupOptionsMenu() {
        resetMenu()
        showMenuFragment(makeupOptionsFragment)
        binding.makeupButton.setBackgroundResource(R.drawable.small_button)
    }

    private fun showAccessoriesMenu() {
        resetMenu()
        showMenuFragment(accessoriesOptionsFragment)
        binding.accessoriesButton.setBackgroundResource(R.drawable.small_button)
    }

    private fun showLooksMenu() {
        resetMenu()
        showMenuFragment(looksOptionsFragment)
        binding.looksButton.setBackgroundResource(R.drawable.small_button)
    }

    private fun resetMenu() {
        for (i in 0 until binding.secondLineButtons.childCount) {
            val child = binding.secondLineButtons.getChildAt(i)
            child.background = null
        }

        hideMenuFragments()
    }


    private fun showMenuFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .show(fragment)
            .commit()
    }

    private fun hideMenuFragments() {
        supportFragmentManager.beginTransaction()
            .hide(makeupOptionsFragment)
            .hide(colorOptionFragment)
            .hide(accessoriesOptionsFragment)
            .hide(looksOptionsFragment)
            .commit()
    }


}
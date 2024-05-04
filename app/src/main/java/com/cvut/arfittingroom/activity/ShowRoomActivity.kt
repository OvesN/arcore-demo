package com.cvut.arfittingroom.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.bumptech.glide.signature.ObjectKey
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.databinding.ActivityShowRoomBinding
import com.cvut.arfittingroom.fragment.AccessoriesOptionsFragment
import com.cvut.arfittingroom.fragment.LooksOptionsFragment
import com.cvut.arfittingroom.fragment.MakeupOptionsFragment
import com.cvut.arfittingroom.model.LOOKS_COLLECTION
import com.cvut.arfittingroom.model.MASK_FRAME_FILE_NAME
import com.cvut.arfittingroom.model.MakeupInfo
import com.cvut.arfittingroom.model.ModelInfo
import com.cvut.arfittingroom.model.enums.ENodeType
import com.cvut.arfittingroom.service.StateService
import com.cvut.arfittingroom.utils.BitmapUtil.replaceNonTransparentPixels
import com.cvut.arfittingroom.utils.FileUtil.deleteTempFiles
import com.cvut.arfittingroom.utils.FileUtil.doesTempAnimatedMaskExist
import com.cvut.arfittingroom.utils.FileUtil.getNextTempMaskFrame
import com.cvut.arfittingroom.utils.FileUtil.getNextTempMaskFrameInputStream
import com.cvut.arfittingroom.utils.FileUtil.getTempMaskTextureBitmap
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gorisse.thomas.sceneform.environment
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.light.build
import com.gorisse.thomas.sceneform.lightEstimationConfig
import com.gorisse.thomas.sceneform.mainLight
import java.util.UUID
import javax.inject.Inject

class ShowRoomActivity : AppCompatActivity(), ResourceListener {
    private val binding: ActivityShowRoomBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private var faceNodeMap = HashMap<AugmentedFace, HashMap<ENodeType, AugmentedFaceNode>>()
    private var isUpdated = false
    private var shouldPlayAnimation = false
    private var gifPrepared = false
    private val gifTextures = mutableListOf<Texture>()
    private var frameCounter = 0
    private var handler = Handler(Looper.getMainLooper())
    private var gifRunnable: Runnable? = null

    private var frameDelay: Long = 100  // Default frame delay (100 ms per frame)
    private val accessoriesOptionsFragment = AccessoriesOptionsFragment()
    private val looksOptionsFragment = LooksOptionsFragment()
    private val makeupOptionsFragment = MakeupOptionsFragment()
    private var shouldClearEditor = false

    // FIXME do not do it like that!
    private var lookId: String = ""
    private var shouldDownloadFormStorage: Boolean = false
    private lateinit var arFragment: ArFrontFacingFragment
    private lateinit var arSceneView: ArSceneView
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    @Inject
    lateinit var stateService: StateService

    override fun onResume() {
        super.onResume()
        resetGifState()

        val bitmap = getTempMaskTextureBitmap(applicationContext)
        bitmap?.let {
            createTextureAndApply(it)
            shouldPlayAnimation = false
        }

        shouldPlayAnimation = doesTempAnimatedMaskExist(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }

        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setContentView(binding.root)

        supportFragmentManager.addFragmentOnAttachListener { fragmentManager: FragmentManager, fragment: Fragment ->
            this.onAttachFragment(
                fragmentManager,
                fragment,
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
        binding.saveButton.setOnClickListener {
            saveLook()
        }
        binding.deleteButton.setOnClickListener {
            clearAll()
        }

        // setupButtonClickListener(binding.buttonLiner, EMakeupType.LINER)
        // setupButtonClickListener(binding.buttonBlush, EMakeupType.BLUSH)
        // setupButtonClickListener(binding.buttonLipstick, EMakeupType.LIPSTICK)
        // setupButtonClickListener(
        // binding.buttonYellowSunglasses,
        // EAccessoryType.YELLOW_GLASSES,
        // EModelType.EYES
        // )
        // setupButtonClickListener(
        // binding.buttonSunglasses,
        // EAccessoryType.SUNGLASSES,
        // EModelType.EYES
        // )
        // setupButtonClickListener(
        // binding.buttonMarioHat,
        // EAccessoryType.MARIO_HAT,
        // EModelType.TOP_HEAD
        // )
        // 
        // binding.button3dEditor.setOnClickListener {
        // editorButtonListener()
        // }

        binding.buttonMaskEditor.setOnClickListener {
            val intent = Intent(this, MakeupEditorActivity::class.java)
            intent.putExtra("shouldClearEditor", shouldClearEditor)
            startActivity(intent)
            finish()
        }
    }

    override fun applyImage(
        type: String,
        ref: String,
        color: Int,
    ) {
        stateService.appliedMakeUpTypes[type] = MakeupInfo(ref, color)

        stateService.appliedMakeUpTypes.values.forEach {
            loadImage(it.ref, it.color)
        }
    }

    override fun removeImage(type: String) {
        val appliedMakeupTypes = stateService.appliedMakeUpTypes
        appliedMakeupTypes.remove(type)

        // Clear faceNode with makeup
        if (appliedMakeupTypes.isEmpty()) {
            faceNodeMap.values
                .mapNotNull { map -> map[ENodeType.MAKEUP] }
                .forEach { node -> node.faceMeshTexture = null }
        } else {
            stateService.appliedMakeUpTypes.values.forEach {
                loadImage(it.ref, it.color)
            }
        }
    }

    override fun applyModel(modelInfo: ModelInfo) {
        stateService.loadedModels[modelInfo.nodeType] = modelInfo
        loadModel(modelInfo.modelRef, modelInfo.nodeType)
    }

    override fun removeModel(type: String) {
        // toggleModelOnFaceNodes(model)
    }

    // FIXME
    override fun applyLook(lookId: String) {
        stopAnimation()
        this.lookId = lookId
        shouldPlayAnimation = true
        shouldDownloadFormStorage = true
    }

    override fun removeLook(lookId: String) {
        TODO("Not yet implemented")
    }

    private fun onAttachFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment,
    ) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFrontFacingFragment
            arFragment.setOnViewCreatedListener { arSceneView: ArSceneView ->
                onViewCreated(
                    arSceneView,
                )
            }
        }
    }

    private fun onViewCreated(arSceneView: ArSceneView) {
        this.arSceneView = arSceneView
        arSceneView.lightEstimationConfig = LightEstimationConfig.DISABLED
        val light =
            LightManager.Builder(LightManager.Type.POINT)
                .position(-0.4f, 0.0f, -0.2f)
                .intensity(200_000.0f)
                .color(0.98f, 0.89f, 0.76f)
                .sunAngularRadius(1.9f)
                .sunHaloSize(10.0f)
                .sunHaloFalloff(80.0f)
                .castShadows(true)
                .build()
        arSceneView.environment?.indirectLight?.intensity = 5000f
        arSceneView.mainLight = light

        // This is important to make sure that the camera stream renders first so that
        // the face mesh occlusion works correctly.
        arSceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST)

        // Check for face detections
        arFragment.setOnAugmentedFaceUpdateListener {
            this.onAugmentedFaceTrackingUpdate()
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

    private fun applyModel(
        renderable: ModelRenderable,
        nodeType: ENodeType,
    ) {
        val sceneView = arFragment.arSceneView
        // Update nodes
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            modelNodesMap[nodeType] =
                AugmentedFaceNode(face).also { node ->
                    node.parent = sceneView.scene
                }.apply { faceRegionsRenderable = renderable }
        }
    }

    private fun updateModelsOnScreen() {
        val sceneView = arFragment.arSceneView
        val modelsToUpdate = stateService.loadedModels
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            isUpdated = true
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            modelsToUpdate.forEach { entry ->
                val faceNode =
                    AugmentedFaceNode(face).also { node ->
                        node.parent = sceneView.scene
                    }
                modelNodesMap[entry.value.nodeType] = faceNode
                // faceNode.faceRegionsRenderable = entry.value.model
                // entry.value.isActive = true

                // makeUpService.loadedModels[entry.key] = entry.value
            }
            stateService.textureBitmap?.let {
                createTextureAndApply(it)
            }

            stateService.loadedModels.clear()
        }
    }

    // FIXME
    private fun prepareAllGifTextures() {
        gifTextures.clear()
        var counter = 0
        var shouldContinue = true

        if (shouldDownloadFormStorage) {
            // FIXME AAAA
            val ref = storage.getReference("$LOOKS_COLLECTION/$lookId/")
            var amont = 0
            ref.listAll().addOnSuccessListener { listResult ->
                amont = listResult.items.size
                repeat(amont) { index ->
                    val fileRef =
                        storage.getReference("$LOOKS_COLLECTION/$lookId/${MASK_FRAME_FILE_NAME}_$index")
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        Texture.builder()
                            .setSource(applicationContext, uri)
                            .build()
                            .thenAccept { texture ->
                                gifTextures.add(texture)
                                shouldContinue = false
                            }
                            .exceptionally { throwable ->
                                Log.println(
                                    Log.ERROR,
                                    null,
                                    "Error creating texture for frame $counter: $throwable",
                                )
                                null
                            }
                    }
                }
            }

            // }
        } else {
            while (shouldContinue) {
                val frameBitmap =
                    getNextTempMaskFrame(applicationContext, counter) ?: break

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
                            "Error creating texture for frame $counter: $throwable",
                        )
                        null
                    }
                counter++
            }
        }

        gifPrepared = true
    }

    private fun createTextureAndApply(combinedBitmap: Bitmap) {
        stateService.textureBitmap = combinedBitmap

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
                val faceNode =
                    AugmentedFaceNode(face).also { node ->
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
        val openGlVersionString =
            (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
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

    private fun loadModel(
        ref: String,
        modelType: ENodeType,
    ) {
        storage.getReference(ref)
            .downloadUrl
            .addOnSuccessListener { uri ->
                ModelRenderable.builder()
                    .setSource(this, uri)
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

        // storage
        // .getReference(ref)
        // .getStream(StreamDownloadTask.StreamProcessor())
        // .addOnSuccessListener {
        // ModelRenderable.builder()
        // .setSource(this) { it.stream }
        // .setIsFilamentGltf(true)
        // .build()
        // .thenAccept { model ->
        // applyModel(model, modelType)
        // }
        // .exceptionally { ex ->
        // Log.println(Log.ERROR, null, ex.message.orEmpty())
        // null
        // }
        // }
        // .addOnFailureListener { ex ->
        // Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
        // Log.println(Log.ERROR, null, ex.message.orEmpty())
        // }
    }


    private fun loadImage(
        ref: String,
        color: Int,
    ) {
        Glide.with(this)
            .asBitmap()
            .signature(ObjectKey(System.currentTimeMillis()))
            .load(storage.getReference(ref))
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        replaceNonTransparentPixels(resource, color)
                        stateService.makeUpBitmaps.add(resource)

                        if (stateService.areMakeupBitmapsPrepared()) {
                            val combinedBitmap = stateService.combineMakeUpBitmaps()
                            combinedBitmap?.let { createTextureAndApply(it) }
                        }
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                },
            )
    }

    private fun toggleModelOnFaceNodes(modelInfo: ModelInfo) {
        val sceneView = arFragment.arSceneView

        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            val faceNode =
                modelNodesMap.getOrPut(modelInfo.nodeType) {
                    AugmentedFaceNode(face).also { node ->
                        node.parent = sceneView.scene
                    }
                }
            // if (faceNode.faceRegionsRenderable == modelInfo.model && modelInfo.isActive) {
            // // If the model is currently applied, remove it
            // faceNode.isEnabled = false
            // modelInfo.isActive = false
            // } else {
            // // If the model is not applied, apply it
            // faceNode.isEnabled = true
            // faceNode.faceRegionsRenderable = modelInfo.model
            // modelInfo.isActive = true
            // }
        }
    }

    private fun startAnimation() {
        if (!gifPrepared) {
            return
        }

        if (gifRunnable == null) {
            gifRunnable =
                Runnable {
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
        gifRunnable = null

        shouldPlayAnimation = false
        gifPrepared = false
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
            val child =
                binding.secondLineButtons.getChildAt(i).apply {
                    background = null
                }
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
            .hide(accessoriesOptionsFragment)
            .hide(looksOptionsFragment)
            .commit()
    }

    // FIXME will not save makeup, models and one frames
    private fun saveLook() {
        val lookId = UUID.randomUUID()
        saveFrames(lookId)

        // TODO should be name
        val data =
            hashMapOf(
                "author" to auth.currentUser?.email,
            )

        fireStore.collection(LOOKS_COLLECTION)
            .document(lookId.toString())
            .set(data)
            .addOnSuccessListener {
                Log.println(
                    Log.INFO,
                    null,
                    "Look $lookId uploaded",
                )
                Toast.makeText(applicationContext, "Look saved successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { ex -> Log.println(Log.ERROR, null, "onFailure: $ex") }
    }

    private fun saveFrames(lookId: UUID) {
        var counter = 0

        while (true) {
            val frameStream = getNextTempMaskFrameInputStream(applicationContext, counter) ?: break
            val ref =
                storage.getReference("$LOOKS_COLLECTION/$lookId/${MASK_FRAME_FILE_NAME}_$counter")

            val uploadTask =
                ref.putStream(frameStream)
                    .addOnSuccessListener { taskSnapshot ->
                    }
            uploadTask.addOnFailureListener {
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
            }

            counter++
        }
    }

    private fun clearAll() {
        stopAnimation()
        deleteTempFiles(applicationContext)
        faceNodeMap.forEach { it.value.forEach { it.value.parent = null } }
        faceNodeMap.clear()
        stateService.clearAll()
        isUpdated = false

        shouldClearEditor = true
    }

    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }

}
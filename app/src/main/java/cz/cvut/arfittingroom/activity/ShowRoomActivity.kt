package cz.cvut.arfittingroom.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFrontFacingFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.gorisse.thomas.sceneform.environment
import com.gorisse.thomas.sceneform.estimatedEnvironmentLights
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.light.build
import com.gorisse.thomas.sceneform.lightEstimationConfig
import com.gorisse.thomas.sceneform.mainLight
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.databinding.ActivityShowRoomBinding
import cz.cvut.arfittingroom.model.ModelInfo
import cz.cvut.arfittingroom.model.enums.EAccessoryType
import cz.cvut.arfittingroom.model.enums.EMakeupType
import cz.cvut.arfittingroom.model.enums.EModelType
import cz.cvut.arfittingroom.service.MakeupService
import cz.cvut.arfittingroom.service.ModelEditorService
import cz.cvut.arfittingroom.utils.FileUtil.doesTempAnimatedMaskExist
import cz.cvut.arfittingroom.utils.FileUtil.getNextTempMaskFrame
import cz.cvut.arfittingroom.utils.FileUtil.getTempMaskTextureBitmap
import cz.cvut.arfittingroom.utils.TextureCombinerUtil.combineDrawables
import mu.KotlinLogging
import javax.inject.Inject

class ShowRoomActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }

    private lateinit var binding: ActivityShowRoomBinding
    private lateinit var arFragment: ArFrontFacingFragment
    private lateinit var arSceneView: ArSceneView
    private var faceNodeMap = HashMap<AugmentedFace, HashMap<EModelType, AugmentedFaceNode>>()
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

        binding = ActivityShowRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.addFragmentOnAttachListener { fragmentManager: FragmentManager, fragment: Fragment ->
            this.onAttachFragment(
                fragmentManager,
                fragment
            )
        }

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFrontFacingFragment::class.java, null)
                    .commit()
            }
        }

        // Set click listeners
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

    private fun setupButtonClickListener(button: Button, makeUpType: EMakeupType) {
        val appliedMakeUpTypes = makeUpService.makeUpState.appliedMakeUpTypes

        button.setOnClickListener {
            if (!appliedMakeUpTypes.add(makeUpType)) appliedMakeUpTypes.remove(makeUpType)
            combineTexturesAndApply()
        }
    }

    private fun editorButtonListener() {
        editorService.loadedModels =
            makeUpService.loadedModels.filter { (_, modelInfo) -> modelInfo.isActive }
                .toMutableMap()

        val intent = Intent(this, ModelEditorActivity::class.java)

        makeUpService.loadedModels.clear()
        startActivity(intent)
    }


    private fun setupButtonClickListener(
        button: Button,
        accessory: EAccessoryType,
        modelType: EModelType
    ) {
        button.setOnClickListener {
            makeUpService.loadedModels[accessory.sourceURI]?.let { model ->
                // If the model is already loaded, toggle its application on the face nodes
                toggleModelOnFaceNodes(model)
            } ?: run {
                // Else, handle the case where the model is not loaded
                loadModel(accessory.sourceURI, modelType)
            }
        }
    }


    private fun applyModel(modelKey: String, modelType: EModelType) {
        val sceneView = arFragment.arSceneView
        // Update nodes
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            // Check if the node for the given modelType already exists
            if (modelNodesMap[modelType] == null) {
                val faceNode = AugmentedFaceNode(face).also { node ->
                    node.parent = sceneView.scene
                }
                modelNodesMap[modelType] = faceNode
                faceNode.faceRegionsRenderable = makeUpService.loadedModels[modelKey]?.model
            } else {
                //FixME too stupid
                makeUpService.loadedModels.values.find { it.model == modelNodesMap[modelType]?.faceRegionsRenderable }?.isActive =
                    false

                modelNodesMap[modelType]?.faceRegionsRenderable =
                    makeUpService.loadedModels[modelKey]?.model
            }
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
                modelNodesMap[entry.value.modelType] = faceNode
                faceNode.faceRegionsRenderable = entry.value.model
                entry.value.isActive = true

                makeUpService.loadedModels[entry.key] = entry.value
            }
            makeUpService.makeUpState.textureBitmap?.let {
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


    private fun combineTexturesAndApply() {
        combineDrawables(makeUpService.makeUpState.appliedMakeUpTypes.map {
            ContextCompat.getDrawable(
                this,
                it.drawableId
            )!!
        }).let {
            if (it != null) {
                createTextureAndApply(it)
            } else {
                //Clean face node makeup texture
                faceNodeMap.values
                    .mapNotNull { map -> map[EModelType.MAKE_UP] }
                    .forEach { node -> node.faceMeshTexture = null }
            }
        }
    }

    private fun createTextureAndApply(combinedBitmap: Bitmap) {
        makeUpService.makeUpState.textureBitmap = combinedBitmap

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
            if (modelNodesMap[EModelType.MAKE_UP] == null) {
                val faceNode = AugmentedFaceNode(face).also { node ->
                    node.parent = sceneView.scene
                }
                modelNodesMap[EModelType.MAKE_UP] = faceNode
                faceNode.faceMeshTexture = texture
            } else {
                modelNodesMap[EModelType.MAKE_UP]?.faceMeshTexture = texture
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

    private fun loadModel(uri: String, modelType: EModelType) {
        // Asynchronously load the model. Once it's loaded, apply it
        ModelRenderable.builder()
            .setSource(this, Uri.parse(uri))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { model ->
                makeUpService.loadedModels[uri] = ModelInfo(modelType, model, uri, true)
                applyModel(uri, modelType)
            }
            .exceptionally { throwable ->
                Log.println(Log.ERROR,null, "Error loading model")
                null
            }
    }

    private fun toggleModelOnFaceNodes(modelInfo: ModelInfo) {
        val sceneView = arFragment.arSceneView

        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            val faceNode = modelNodesMap.getOrPut(modelInfo.modelType) {
                AugmentedFaceNode(face).also { node ->
                    node.parent = sceneView.scene
                }
            }

            if (faceNode.faceRegionsRenderable == modelInfo.model && modelInfo.isActive) {
                // If the model is currently applied, remove it
                faceNode.isEnabled = false
                modelInfo.isActive = false
            } else {
                // If the model is not applied, apply it
                faceNode.isEnabled = true
                faceNode.faceRegionsRenderable = modelInfo.model
                modelInfo.isActive = true
            }
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
}
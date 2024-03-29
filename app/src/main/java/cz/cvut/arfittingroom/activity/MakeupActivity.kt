package cz.cvut.arfittingroom.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.AugmentedFaceNode
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.databinding.ActivityMakeupBinding
import cz.cvut.arfittingroom.fragment.FaceArFragment
import cz.cvut.arfittingroom.model.ModelInfo
import cz.cvut.arfittingroom.model.enums.EAccessoryType
import cz.cvut.arfittingroom.model.enums.EMakeupType
import cz.cvut.arfittingroom.model.enums.EModelType
import cz.cvut.arfittingroom.service.MakeupService
import cz.cvut.arfittingroom.service.ModelEditorService
import cz.cvut.arfittingroom.utils.TextureCombinerUtil.combineDrawables
import mu.KotlinLogging
import javax.inject.Inject

class MakeupActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
        private val logger = KotlinLogging.logger { }
    }

    private lateinit var binding: ActivityMakeupBinding
    private lateinit var arFragment: FaceArFragment
    private var faceNodeMap = HashMap<AugmentedFace, HashMap<EModelType, AugmentedFaceNode>>()
    private var isUpdated = false

    @Inject
    lateinit var makeUpService: MakeupService

    @Inject
    lateinit var editorService: ModelEditorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }

        // Inflate the layout for this activity
        binding = ActivityMakeupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use the FragmentManager to find the AR Fragment by ID
        arFragment = supportFragmentManager.findFragmentById(R.id.face_fragment) as FaceArFragment

        val sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        val scene = sceneView.scene

        // Set click listeners
        setupButtonClickListener(binding.buttonLiner, EMakeupType.LINER)
        setupButtonClickListener(binding.buttonBlush, EMakeupType.BLUSH)
        setupButtonClickListener(binding.buttonLipstick, EMakeupType.LIPSTICK)
        setupButtonClickListener(
            binding.buttonYellowSunglasses,
            EAccessoryType.YELLOW_GLASSES,
            EModelType.EYES
        )
        setupButtonClickListener(
            binding.buttonSunglasses,
            EAccessoryType.SUNGLASSES,
            EModelType.EYES
        )
        setupButtonClickListener(
            binding.buttonMarioHat,
            EAccessoryType.MARIO_HAT,
            EModelType.TOP_HEAD
        )

        binding.button3dEditor.setOnClickListener {
            editorButtonListener()
        }

        binding.buttonMakeupEditor.setOnClickListener {
            val intent = Intent(this, MakeupEditorActivity::class.java)
            startActivity(intent)
        }

        scene.addOnUpdateListener {
            // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking
            faceNodeMap.entries.removeIf { (face, nodes) ->
                if (face.trackingState == TrackingState.STOPPED) {
                    nodes.forEach { entry -> entry.value.setParent(null) }
                    true
                } else {
                    false
                }
            }
            if (!isUpdated) {
                updateModelsOnScreen()
            }
        }
    }


    private fun setupButtonClickListener(button: Button, makeUpType: EMakeupType) {
        val appliedMakeUpTypes = makeUpService.makeUpState.appliedMakeUpTypes

        button.setOnClickListener {
            logger.info { "${makeUpType.name} button clicked" }
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
            logger.info { "${accessory.name} button clicked" }
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
                    node.setParent(sceneView.scene)
                }
                modelNodesMap[modelType] = faceNode
                faceNode.faceRegionsRenderable = makeUpService.loadedModels[modelKey]?.model
            } else {
                //FixME too stupid
                makeUpService.loadedModels.values.find { it.model == modelNodesMap[modelType]?.faceRegionsRenderable }?.isActive = false

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
                    node.setParent(sceneView.scene)

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
            .exceptionally { throwable ->
                logger.error { "Error creating texture from bitmap: $throwable" }
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
                    node.setParent(sceneView.scene)
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
            .build()
            .thenAccept { model ->
                makeUpService.loadedModels[uri] = ModelInfo(modelType, model, uri, true)
                applyModel(uri, modelType)
            }
            .exceptionally { throwable ->
                logger.error(throwable) { "Error loading model." }
                null
            }
    }

    private fun toggleModelOnFaceNodes(modelInfo: ModelInfo) {
        val sceneView = arFragment.arSceneView

        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            val faceNode = modelNodesMap.getOrPut(modelInfo.modelType) {
                AugmentedFaceNode(face).also { node ->
                    node.setParent(sceneView.scene)
                }
            }

            if (faceNode.faceRegionsRenderable == modelInfo.model) {
                // If the model is currently applied, remove it
                faceNode.faceRegionsRenderable = null
                modelInfo.isActive = false
            } else {
                // If the model is not applied, apply it
                faceNode.faceRegionsRenderable = modelInfo.model
                modelInfo.isActive = true
            }
        }
    }
}
package cz.cvut.arfittingroom

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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
import cz.cvut.arfittingroom.databinding.ActivityMakeupBinding
import cz.cvut.arfittingroom.model.enums.EAccessoryType
import cz.cvut.arfittingroom.model.enums.EMakeupType
import cz.cvut.arfittingroom.model.enums.EModelType
import mu.KotlinLogging

class MakeupActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
        private val logger = KotlinLogging.logger { }
    }

    private lateinit var binding: ActivityMakeupBinding

    private val appliedMakeUpTypes = mutableSetOf<EMakeupType>()
    private var loadedModels = mutableMapOf<String, ModelRenderable>()
    private lateinit var arFragment: FaceArFragment
    private var faceNodeMap = HashMap<AugmentedFace, HashMap<EModelType, AugmentedFaceNode>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        }
    }

    private fun setupButtonClickListener(button: Button, makeUpType: EMakeupType) {
        button.setOnClickListener {
            logger.info { "${makeUpType.name} button clicked" }
            if (!appliedMakeUpTypes.add(makeUpType)) appliedMakeUpTypes.remove(makeUpType)
            combineTexturesAndApply()
        }
    }

    private fun setupButtonClickListener(
        button: Button,
        accessory: EAccessoryType,
        modelType: EModelType
    ) {
        button.setOnClickListener {
            if (loadedModels.containsKey(accessory.sourceURI)) {
                // If the model is already loaded, toggle its application on the face node
                toggleModelOnFaceNodes(accessory.sourceURI, modelType)
            } else {
                // If the model is not loaded, load it
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
                faceNode.faceRegionsRenderable = loadedModels[modelKey]
            } else {
                modelNodesMap[modelType]?.faceRegionsRenderable = loadedModels[modelKey]
            }
        }
    }


    private fun combineTexturesAndApply() {
        //TODO FIX NULLABILITY
        combineDrawables(appliedMakeUpTypes.map {
            ContextCompat.getDrawable(
                this,
                it.drawableId
            )!!
        }).let {
            if (it != null) {
                createTexture(it)
            } else {
                //Clean face node makeup texture
                faceNodeMap.values
                    .mapNotNull { map -> map[EModelType.MAKE_UP] }
                    .forEach { node -> node.faceMeshTexture = null }
            }
        }
    }

    // Combine two drawables into a single Bitmap
    private fun combineDrawables(layers: List<Drawable>): Bitmap? {
        if (layers.isEmpty()) return null

        val bitmap = Bitmap.createBitmap(
            layers.first().intrinsicWidth,
            layers.first().intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        layers.forEach { it.setBounds(0, 0, canvas.width, canvas.height) }
        layers.forEach { it.draw(canvas) }

        return bitmap
    }

    private fun createTexture(combinedBitmap: Bitmap) {
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
                loadedModels[uri] = model
                applyModel(uri, modelType)
            }
            .exceptionally { throwable ->
                logger.error(throwable) { "Error loading model." }
                null
            }
    }

    private fun toggleModelOnFaceNodes(modelKey: String, modelType: EModelType) {
        val sceneView = arFragment.arSceneView

        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }
            val faceNode = modelNodesMap.getOrPut(modelType) {
                AugmentedFaceNode(face).also { node ->
                    node.setParent(sceneView.scene)
                }
            }

            if (faceNode.faceRegionsRenderable == loadedModels[modelKey]) {
                // If the model is currently applied, remove it
                faceNode.faceRegionsRenderable = null
            } else {
                // If the model is not applied, apply it
                faceNode.faceRegionsRenderable = loadedModels[modelKey]
            }
        }
    }
}
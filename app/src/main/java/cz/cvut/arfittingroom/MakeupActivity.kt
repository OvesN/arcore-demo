package cz.cvut.arfittingroom

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.AugmentedFaceNode
import cz.cvut.arfittingroom.databinding.ActivityGlassesBinding
import cz.cvut.arfittingroom.databinding.ActivityMakeupBinding
import mu.KotlinLogging

class MakeupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMakeupBinding

    private val appliedMakeUpTypes = mutableSetOf<EMakeUpType>()

    companion object {
        const val MIN_OPENGL_VERSION = 3.0
        private val logger = KotlinLogging.logger { }
    }

    private lateinit var arFragment: FaceArFragment
    private var faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    enum class EMakeUpType(val drawableId: Int) {
        LINER(R.drawable.liner),
        BLUSH(R.drawable.blush),
        LIPSTICK(R.drawable.lipstick)
    }

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
        setupButtonClickListener(binding.buttonLiner, EMakeUpType.LINER)
        setupButtonClickListener(binding.buttonBlush, EMakeUpType.BLUSH)
        setupButtonClickListener(binding.buttonLipstick, EMakeUpType.LIPSTICK)

        scene.addOnUpdateListener {
            sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
                logger.info { face.centerPose }
            }

            // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking
            faceNodeMap.entries.removeIf { (face, nodes) ->
                if (face.trackingState == TrackingState.STOPPED) {
                    nodes.setParent(null)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupButtonClickListener(button: Button, makeUpType: EMakeUpType) {
        button.setOnClickListener {
            logger.info { "${makeUpType.name} button clicked" }
            if (!appliedMakeUpTypes.add(makeUpType)) appliedMakeUpTypes.remove(makeUpType)
            combineTexturesAndApply()
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
            }
            else {
                //Clean face node texture
                faceNodeMap.entries.forEach{ it.value.faceMeshTexture = null}
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
            .thenAccept{texture -> applyTextureToAllFaces(texture)}
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
            val faceNode = faceNodeMap.getOrPut(face) {
                AugmentedFaceNode(face).also { node ->
                    node.setParent(sceneView.scene)
                }
            }
            faceNode.faceMeshTexture = texture
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

        openGlVersionString?.let { s ->
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
                finish()
                return false
            }
        }
        return true
    }
}
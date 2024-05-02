package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.graphics.Color.LTGRAY
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.controller.DragTransformableNode
import cz.cvut.arfittingroom.databinding.ActivityModelEditorBinding
import cz.cvut.arfittingroom.service.ModelEditorService
import javax.inject.Inject

class ModelEditorActivity : AppCompatActivity() {
    @Inject
    lateinit var editorService: ModelEditorService
    private val binding: ActivityModelEditorBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var sceneView: SceneView
    private lateinit var transformationSystem: TransformationSystem
    private lateinit var modelNode: DragTransformableNode
    private var selectedModelKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        //making a transformation system so we can interact with the 3d model
        transformationSystem =
            TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())

        setContentView(binding.root)
        sceneView = binding.sceneView

        val buttonContainer = binding.buttonContainer

        editorService.loadedModels.forEach{ model ->
            val button = Button(this).apply {
                text = model.key
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    handleChooseModelButtonClick(model.key)
                }
            }
            buttonContainer.addView(button)
        }

        setUpScene(sceneView)
        addModelToScene()

        sceneView.scene
            .addOnPeekTouchListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
                transformationSystem.onTouch(
                    hitTestResult,
                    motionEvent
                )
            }

        binding.buttonChangeColor.setOnClickListener {
            showColorPicker()
        }

        binding.buttonBack.setOnClickListener{
            val intent = Intent(this, ShowRoomActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleChooseModelButtonClick(modelKey: String) {
//        selectedModelKey = modelKey
//        editorService.loadedModels[modelKey]?.model?.let { updateModelView(it) }
    }

    private fun addModelToScene() {
        if (editorService.loadedModels.isNotEmpty()) {
            val model = editorService.loadedModels.entries.first()

//            this.modelNode = DragTransformableNode(transformationSystem).apply {
//                setParent(sceneView.scene)
//                translationController.isEnabled = false
//                scaleController.isEnabled = true
//                scaleController.minScale = 0.01f
//                scaleController.maxScale = 2f
//                rotationController.isEnabled = true
//                localPosition = Vector3(0f, 0f, -2.3f)
//                renderable = model.value.model
//            }

            selectedModelKey = model.key
        }
    }

    private fun showColorPicker() {
        val colorOptions = arrayOf("Red", "Green", "Blue") // Add more colors as needed
        val colorValues = mapOf("Red" to Color(255f,0f,0f), "Green" to Color(0f,255f,0f), "Blue" to Color(0f,255f,255f))

        AlertDialog.Builder(this)
            .setTitle("Choose a color")
            .setItems(colorOptions) { _, which ->
                val colorName = colorOptions[which]
                val selectedColor = colorValues[colorName]
                selectedColor?.let { saveSelectedColor(it) }
            }
            .show()
    }

    private fun saveSelectedColor(color: Color) {
//        val newModel = editorService.changeColor(selectedModelKey, color, 0)
//        updateModelView(newModel)
    }

    private fun updateModelView(model: ModelRenderable) {
        modelNode.renderable = model
    }

    private fun setUpScene(sceneView: SceneView) {
        sceneView.renderer?.setClearColor(Color(LTGRAY))

        sceneView.scene.camera.localPosition = Vector3(0f, 0f, -2.18f)

        val light = Light.builder(Light.Type.DIRECTIONAL)
            .setColor(Color(1.0f, 1.0f, 1.0f)) // White light
            .setShadowCastingEnabled(true)
            .build()

        val lightNode = Node().apply {
            this.light = light
            localPosition = Vector3(0f, 5f, -5f) // Adjust position as needed
        }

        sceneView.scene.addChild(lightNode)
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        sceneView.destroy()
    }
}

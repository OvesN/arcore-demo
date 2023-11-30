package cz.cvut.arfittingroom.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.databinding.ActivityModelEditorBinding
import cz.cvut.arfittingroom.service.ModelEditorService
import javax.inject.Inject

class ModelEditorActivity : AppCompatActivity() {
    @Inject
    lateinit var editorService: ModelEditorService
    private lateinit var binding: ActivityModelEditorBinding
    private lateinit var sceneView: SceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        // Inflate the layout for this activity
        binding = ActivityModelEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sceneView = binding.sceneView
        addModelToScene()
    }


    private fun addModelToScene() {
        if (editorService.modelsToShow.isNotEmpty()) {
            val modelNode = Node().apply {
                renderable = editorService.modelsToShow.values.first()
            }
            sceneView.scene.addChild(modelNode)
        }

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
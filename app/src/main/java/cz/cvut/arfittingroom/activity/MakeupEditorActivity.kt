package cz.cvut.arfittingroom.activity

import android.R.string.ok
import android.R.string.cancel
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.databinding.ActivityMakeupEditorBinding
import cz.cvut.arfittingroom.draw.DrawView
import cz.cvut.arfittingroom.draw.model.enums.ELayerEditAction
import cz.cvut.arfittingroom.draw.model.enums.EShape
import cz.cvut.arfittingroom.service.MakeupService
import javax.inject.Inject


private val SELECTED_COLOR = Color.parseColor("#FF5722")
private const val DEFAULT_COLOR = Color.TRANSPARENT

class MakeupEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMakeupEditorBinding
    private lateinit var drawView: DrawView
    private lateinit var imageView: ImageView
    private lateinit var slider: Slider
    private lateinit var layersButtonsContainer: LinearLayout

    @Inject
    lateinit var makeUpService: MakeupService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        // Inflate the layout for this activity
        binding = ActivityMakeupEditorBinding.inflate(layoutInflater)
        drawView = binding.drawView
        imageView = binding.faceImage
        slider = binding.strokeSizeSlider
        layersButtonsContainer = binding.dynamicLayerButtonsContainer
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            drawView.saveBitmap {
                // This code will be executed after the bitmap is saved
                val intent = Intent(this, MakeupActivity::class.java)
                startActivity(intent)
            }
        }
        binding.buttonColorPicker.setOnClickListener {
            showColorPickerDialog()
        }
        binding.buttonRedo.setOnClickListener {
            drawView.redo()
        }
        binding.buttonUndo.setOnClickListener {
            drawView.undo()
        }
        binding.buttonStar.setOnClickListener {
            toggleStrokeShape(EShape.STAR)
        }
        binding.buttonHeart.setOnClickListener {
            toggleStrokeShape(EShape.HEART)
        }
        binding.buttonFlowersImage.setOnClickListener {
            toggleImage(R.drawable.flowers)
        }
        slider.addOnChangeListener { _, value, _ ->
            drawView.setStrokeWidth(value)
        }
        drawView.setStrokeWidth(slider.value)

        //TODO fix num or indexes or whut
        binding.buttonAddLayer.setOnClickListener {
            updateLayersButtons(drawView.addLayer() + 1)
        }

        drawView.setOnLayerInitializedListener(object : DrawView.OnLayerInitializedListener {
            override fun onLayerInitialized(numOfLayers: Int) {
                updateLayersButtons(numOfLayers)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        updateLayersButtons(drawView.layerManager.getNumOfLayers())
    }

    // Create buttons for layers in reverse order and select the active one
    private fun updateLayersButtons(numOfLayers: Int) {
        layersButtonsContainer.removeAllViews()

        for (i in numOfLayers - 1 downTo 0) {
            val button = Button(this).apply {
                text = i.toString()
                setOnClickListener { showLayerEditDialog(i, this) }
            }

            if (i == drawView.layerManager.activeLayerIndex) {
                button.setBackgroundColor(SELECTED_COLOR)
            }
            else {
                button.setBackgroundColor(DEFAULT_COLOR)
            }

            layersButtonsContainer.addView(button, layersButtonsContainer.childCount)
        }
    }

    private fun showLayerEditDialog(layerIndex: Int, button: Button) {
        val options = ELayerEditAction.entries.map { it.string }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Edit layer $layerIndex")
            .setItems(options) { _, which ->

                when (options[which]) {
                    ELayerEditAction.DELETE.string -> {
                        drawView.removeLayer(layerIndex)
                    }

                    ELayerEditAction.MOVE_DOWN.string -> {
                        drawView.moveLayer(layerIndex, layerIndex - 1)
                    }

                    ELayerEditAction.MOVE_UP.string -> {
                        drawView.moveLayer(layerIndex, layerIndex + 1)
                    }

                    ELayerEditAction.SELECT.string -> {
                        drawView.setActiveLayer(layerIndex)
                    }
                }

                updateLayersButtons(drawView.layerManager.getNumOfLayers())
            }
            .show()
    }

    private fun toggleStrokeShape(shape: EShape) {
        drawView.layerManager.deselectAllElements()
        drawView.strokeShape = if (drawView.strokeShape == shape) EShape.NONE else shape
        drawView.selectedElement = null
    }

    private fun toggleImage(imageId: Int) {
        drawView.loadImage(imageId)
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog.Builder(this)
            .setTitle("ColorPicker Dialog")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(getString(ok),
                ColorEnvelopeListener { envelope, _ -> drawView.setColor(envelope.color) })
            .setNegativeButton(
                getString(cancel)
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }


}
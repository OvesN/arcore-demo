package com.cvut.arfittingroom.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.databinding.ActivityMakeupEditorBinding
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.model.enums.ELayerEditAction
import com.cvut.arfittingroom.draw.model.enums.EShape
import com.cvut.arfittingroom.utils.UIUtil.showColorPickerDialog
import com.google.android.material.slider.Slider

private const val DEFAULT_COLOR = Color.TRANSPARENT

private val SELECTED_COLOR = Color.parseColor("#FF5722")

class MakeupEditorActivity : AppCompatActivity() {
    private val binding: ActivityMakeupEditorBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var drawView: DrawView
    private lateinit var imageView: ImageView
    private lateinit var slider: Slider
    private lateinit var layersButtonsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        drawView = binding.drawView
        imageView = binding.faceImage
        slider = binding.strokeSizeSlider
        layersButtonsContainer = binding.dynamicLayerButtonsContainer
        setContentView(binding.root)

        if (intent.getBooleanExtra("shouldClearEditor", false)) {
            drawView.clearCanvas()
        }

        binding.buttonBack.setOnClickListener {
            drawView.saveBitmap {
                // This code will be executed after the bitmap is saved
                startActivity(Intent(this, ShowRoomActivity::class.java))
                finish()
            }
        }
        binding.buttonColorPicker.setOnClickListener {
            showColorPickerDialog(this) { envelopColor ->
                drawView.setColor(
                    envelopColor,
                )
            }
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
            addImage(R.drawable.flowers)
        }
        binding.buttonStar.setOnClickListener {
            toggleStrokeShape(EShape.STAR)
        }
        binding.buttonHeart.setOnClickListener {
            toggleStrokeShape(EShape.HEART)
        }
        binding.buttonFlowersImage.setOnClickListener {
            addImage(R.drawable.flowers)
        }
        binding.buttonGif.setOnClickListener {
            addGif(R.drawable.donut)
        }
        binding.buttonGif2.setOnClickListener {
            addGif(R.drawable.hamburger)
        }
        slider.addOnChangeListener { _, value, _ ->
            drawView.setStrokeWidth(value)
        }
        drawView.setStrokeWidth(slider.value)

        // TODO fix num or indexes or whut
        binding.buttonAddLayer.setOnClickListener {
            updateLayersButtons(drawView.addLayer() + 1)
        }

        drawView.setOnLayerInitializedListener(
            object : DrawView.OnLayerInitializedListener {
                override fun onLayerInitialized(numOfLayers: Int) {
                    updateLayersButtons(numOfLayers)
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()

        drawView.layerManager.resetAllGifs()
        drawView.layerManager.setAllGifsToStaticMode()

        updateLayersButtons(drawView.layerManager.getNumOfLayers())
    }

    override fun onPause() {
        super.onPause()
        drawView.stopAnimation()
    }

    // Create buttons for layers in reverse order and select the active one
    private fun updateLayersButtons(numOfLayers: Int) {
        layersButtonsContainer.removeAllViews()

        for (i in numOfLayers - 1 downTo 0) {
            val button =
                Button(this).apply {
                    text = i.toString()
                    setOnClickListener { showLayerEditDialog(i, this) }
                }

            if (i == drawView.layerManager.getActiveLayerIndex()) {
                button.setBackgroundColor(SELECTED_COLOR)
            } else {
                button.setBackgroundColor(DEFAULT_COLOR)
            }

            layersButtonsContainer.addView(button, layersButtonsContainer.childCount)
        }
    }

    private fun showLayerEditDialog(
        layerIndex: Int,
        button: Button,
    ) {
        val options = ELayerEditAction.entries.map { it.string }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Edit layer $layerIndex")
            .setItems(options) { _, which ->

                when (options[which]) {
                    ELayerEditAction.DELETE.string -> drawView.removeLayer(layerIndex)

                    ELayerEditAction.MOVE_DOWN.string ->
                        drawView.moveLayer(
                            layerIndex,
                            layerIndex - 1,
                        )

                    ELayerEditAction.MOVE_UP.string ->
                        drawView.moveLayer(
                            layerIndex,
                            layerIndex + 1,
                        )

                    ELayerEditAction.SELECT.string -> drawView.setActiveLayer(layerIndex)
                }

                // updateLayersButtons(drawView.layerManager.getNumOfLayers())
            }
            .show()
    }

    private fun toggleStrokeShape(shape: EShape) {
        drawView.layerManager.deselectAllElements()
        drawView.strokeShape = if (drawView.strokeShape == shape) EShape.NONE else shape
        drawView.selectedElement = null
    }

    private fun addImage(imageId: Int) {
        drawView.loadImage(imageId)
    }

    private fun addGif(gifId: Int) {
        drawView.loadGif(gifId)
    }
}

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
    private var selectedLayerButton: Button? = null

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
            adjustAndSaveBitmap()
            val intent = Intent(this, MakeupActivity::class.java)
            startActivity(intent)
        }
        binding.buttonColorPicker.setOnClickListener {
            showColorPickerDialog()
        }
        binding.buttonRedo.setOnClickListener {
            drawView.layerManager.redo()
        }
        binding.buttonUndo.setOnClickListener {
            drawView.layerManager.undo()
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

        //TODO fix later, what if we already have layers?
        updateLayersButtons(1)
    }

    private fun updateLayersButtons(numOfLayers: Int) {
        layersButtonsContainer.removeAllViews()  // Clear existing views if any

        for (i in 0 until   numOfLayers) {
            val button = Button(this).apply {
                text = i.toString()
                setOnClickListener { showLayerEditDialog(i, this) }
            }

            selectLayerButton(button)
            layersButtonsContainer.addView(button)
        }
    }

    private fun showLayerEditDialog(layerIndex: Int, button: Button) {
        val options = ELayerEditAction.entries.map { it.string }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Edit layer $layerIndex")
            .setItems(options) { _, which ->

                when (options[which]) {
                    ELayerEditAction.DELETE.string -> {
                        drawView.layerManager.removeLayer(layerIndex)
                        val newLayerIndex = if (layerIndex == 0) 0 else layerIndex - 1
                        drawView.setActiveLayer(newLayerIndex)
                        selectLayerButton(layersButtonsContainer.getChildAt(newLayerIndex) as Button)
                    }

                    ELayerEditAction.MOVE_DOWN.string -> {
                        drawView.layerManager.moveLayer(layerIndex, layerIndex + 1)
                        if (drawView.setActiveLayer(layerIndex + 1))
                        {
                            selectLayerButton(layersButtonsContainer.getChildAt(layerIndex) as Button)
                        }
                    }

                    ELayerEditAction.MOVE_UP.string -> {
                        drawView.layerManager.moveLayer(layerIndex, layerIndex - 1)
                        if (drawView.setActiveLayer(layerIndex - 1)) {
                            selectLayerButton(layersButtonsContainer.getChildAt(layerIndex - 1) as Button)
                        }
                    }

                    ELayerEditAction.SELECT.string -> {
                        drawView.setActiveLayer(layerIndex)
                        selectLayerButton(button)
                    }
                }
            }
            .show()

    }

    private fun selectLayerButton(button: Button) {
        button.setBackgroundColor(SELECTED_COLOR)
        if (button == selectedLayerButton) {
            return
        }
        selectedLayerButton?.setBackgroundColor(DEFAULT_COLOR)
        selectedLayerButton = button
    }

    private fun toggleStrokeShape(shape: EShape) {
        drawView.isInImageMode = false
        drawView.strokeShape = if (drawView.strokeShape == shape) EShape.CIRCLE else shape
    }

    private fun toggleImage(imageId: Int) {
        drawView.isInImageMode = drawView.isInImageMode != true
        if (drawView.isInImageMode) {
            drawView.drawImage(imageId)
        }
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

    private fun adjustAndSaveBitmap() {
        val bitmap = adjustBitmap()
        Bitmap.createScaledBitmap(bitmap, 1024, 1024, true)
        makeUpService.makeUpState.textureBitmap = bitmap
    }

    private fun adjustBitmap(): Bitmap {
        // Calculate the dimensions for the square crop
        val width = imageView.width
        val height = imageView.height
        val newY = (height - width) / 2

        // Crop the bitmap
        val croppedBitmap = Bitmap.createBitmap(drawView.getBitmap(), 0, newY, width, width)

        // Create a matrix for the mirroring transformation
        val matrix = Matrix().apply {
            postScale(
                -1f,
                1f,
                croppedBitmap.width / 2f,
                croppedBitmap.height / 2f
            )
        }

        // Create and return the mirrored bitmap
        return Bitmap.createBitmap(
            croppedBitmap,
            0,
            0,
            croppedBitmap.width,
            croppedBitmap.height,
            matrix,
            true
        )
    }

}
package cz.cvut.arfittingroom.activity

import android.R.string.ok
import android.R.string.cancel
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.databinding.ActivityMakeupEditorBinding
import cz.cvut.arfittingroom.draw.DrawView
import cz.cvut.arfittingroom.draw.EShape
import cz.cvut.arfittingroom.service.MakeupEditorService
import cz.cvut.arfittingroom.service.MakeupService
import javax.inject.Inject


class MakeupEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMakeupEditorBinding
    private lateinit var drawView: DrawView
    private lateinit var imageView: ImageView
    private lateinit var slider: Slider

    @Inject
    lateinit var makeUpService: MakeupService
    @Inject
    lateinit var makeupEditorService: MakeupEditorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        // Inflate the layout for this activity
        binding = ActivityMakeupEditorBinding.inflate(layoutInflater)
        drawView = binding.drawView
        imageView = binding.faceImage
        slider = binding.strokeSizeSlider

        setContentView(binding.root)

        binding.buttonBack.setOnClickListener{
            adjustAndSaveBitmap()
            val intent = Intent(this, MakeupActivity::class.java)
            startActivity(intent)
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
        slider.addOnChangeListener { _, value, _ ->
            drawView.setStrokeWidth(value)
        }
        drawView.setStrokeWidth(slider.value)

    }

    private fun toggleStrokeShape(shape: EShape) {
        drawView.strokeShape = if (drawView.strokeShape == shape) EShape.CIRCLE else shape
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
        val matrix = Matrix().apply { postScale(-1f, 1f, croppedBitmap.width / 2f, croppedBitmap.height / 2f) }

        // Create and return the mirrored bitmap
        return Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true)
    }

}
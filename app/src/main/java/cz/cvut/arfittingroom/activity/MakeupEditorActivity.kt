package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.databinding.ActivityMakeupEditorBinding
import cz.cvut.arfittingroom.service.MakeupEditorService
import cz.cvut.arfittingroom.service.MakeupService
import cz.cvut.arfittingroom.view.DrawView
import javax.inject.Inject

class MakeupEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMakeupEditorBinding
    private lateinit var drawView: DrawView
    private lateinit var imageView: ImageView

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

        setContentView(binding.root)

        binding.buttonBack.setOnClickListener{
            adjustAndSaveBitmap()
            val intent = Intent(this, MakeupActivity::class.java)
            startActivity(intent)
        }
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
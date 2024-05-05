package com.cvut.arfittingroom.fragment

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.UIChangeListener
import com.cvut.arfittingroom.model.enums.ECameraMode
import com.google.ar.core.Frame
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.ArFrontFacingFragment
import java.io.IOException


class CameraModeFragment : Fragment() {
    private var activeCameraMode = ECameraMode.PHOTO
    private lateinit var videoButton: Button
    private lateinit var photoButton: Button
    lateinit var arFragment: ArFrontFacingFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_camera_mode, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.return_button).setOnClickListener {
            showMainLayout()
        }

        videoButton = view.findViewById(R.id.video_button)
        photoButton = view.findViewById(R.id.photo_button)

        videoButton.setOnClickListener {
            activeCameraMode = ECameraMode.VIDEO
            it.background = ContextCompat.getDrawable(requireContext(), R.drawable.small_button)
            photoButton.background = null
        }

        photoButton.setOnClickListener {
            activeCameraMode = ECameraMode.PHOTO
            it.background = ContextCompat.getDrawable(requireContext(), R.drawable.small_button)
            videoButton.background = null
        }

        view.findViewById<ImageButton>(R.id.camera_button).setOnClickListener {
            when (activeCameraMode) {
                ECameraMode.PHOTO -> takePhoto()
                ECameraMode.VIDEO

                -> {
                }
            }

        }
    }

    private fun showMainLayout() {
        val listener = context as? UIChangeListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        listener.showMainLayout()
    }

    private fun takePhoto() {
        val bitmap = Bitmap.createBitmap(
            arFragment.requireView().width,
            arFragment.requireView().height,
            Bitmap.Config.ARGB_8888
        )
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        PixelCopy.request(arFragment.arSceneView, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap)
                } catch (e: IOException) {
                    Toast.makeText(
                        requireContext(), e.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return@request
                }
                Toast.makeText(
                    requireContext(), "Photo saved in gallery",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    requireContext(), "Failed to take a photo",
                    Toast.LENGTH_LONG
                ).show()
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))

    }


    private fun saveBitmapToDisk(bitmap: Bitmap) {

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "AR_Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ARtist")
        }

        val uri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        uri?.let {
            requireContext().contentResolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }
    }
}
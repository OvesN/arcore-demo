package com.cvut.arfittingroom.fragment

import android.content.ContentValues
import android.graphics.Bitmap
import android.media.CamcorderProfile
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.UIChangeListener
import com.cvut.arfittingroom.model.enums.ECameraMode
import com.cvut.arfittingroom.service.VideoRecorder
import com.cvut.arfittingroom.utils.UIUtil.animateButton
import com.google.ar.sceneform.ux.ArFrontFacingFragment
import io.github.muddz.styleabletoast.StyleableToast
import java.io.IOException

/**
 * Provides functionality for taking photos and recording videos
 *
 * @author Veronika Ovsyannikova
 */
class CameraModeFragment : Fragment() {
    private var activeCameraMode = ECameraMode.PHOTO
    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private var startTime: Long = 0
    private var arFragment: ArFrontFacingFragment? = null
    private val videoRecorder = VideoRecorder()
    private lateinit var videoButton: Button
    private lateinit var photoButton: Button
    private lateinit var cameraButton: ImageButton
    private lateinit var timer: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_camera_mode, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val orientation = resources.configuration.orientation
        videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation)

        view.findViewById<ImageButton>(R.id.return_button).setOnClickListener {
            showMainLayout()
        }

        videoButton = view.findViewById(R.id.video_button)
        photoButton = view.findViewById(R.id.photo_button)
        cameraButton = view.findViewById(R.id.camera_button)
        timer = view.findViewById(R.id.timer)

        photoButton.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.small_button)

        videoButton.setOnClickListener {
            activeCameraMode = ECameraMode.VIDEO
            it.background = ContextCompat.getDrawable(requireContext(), R.drawable.small_button)
            cameraButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.video_button)
            photoButton.background = null
        }

        photoButton.setOnClickListener {
            activeCameraMode = ECameraMode.PHOTO
            it.background = ContextCompat.getDrawable(requireContext(), R.drawable.small_button)
            cameraButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.circle)
            videoButton.background = null
        }

        cameraButton.setOnClickListener { button ->
            when (activeCameraMode) {
                ECameraMode.PHOTO ->
                    arFragment?.let {
                        animateButton(button)
                        takePhoto(it)
                    }

                ECameraMode.VIDEO -> {
                    animateButton(button)
                    toggleRecording()
                }
            }
        }
    }

    fun setARFragment(arFragment: ArFrontFacingFragment) {
        this.arFragment = arFragment
        videoRecorder.setSceneView(arFragment.arSceneView)
    }

    private fun toggleRecording() {
        val recording = videoRecorder.onToggleRecord()
        if (recording) {
            startTimer()
            cameraButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.stop_button)
        } else {
            stopTimer()
            cameraButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.video_button)

            val videoPath = videoRecorder.videoPath.absolutePath

            val values = ContentValues()
            values.put(MediaStore.Video.Media.TITLE, "GlamARtist Video")
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATA, videoPath)

            requireContext().contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values,
            )

            // This will make file visible in gallery
            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(videoPath),
                null,
                null,
            )

            StyleableToast.makeText(requireContext(), "Video saved in gallery", R.style.mytoast)
                .show()
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

    private fun startTimer() {
        timer.visibility = View.VISIBLE
        timerHandler = Handler(Looper.getMainLooper())
        startTime = System.currentTimeMillis()
        timerRunnable =
            object : Runnable {
                override fun run() {
                    val millis = System.currentTimeMillis() - startTime
                    val seconds = (millis / 1000) % 60
                    val minutes = (millis / (1000 * 60)) % 60
                    val hours = (millis / (1000 * 60 * 60)) % 24
                    timer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    timerHandler?.postDelayed(
                        this,
                        500,
                    )
                }
            }
        timerRunnable?.let { timerHandler?.postDelayed(it, 0) }
    }

    private fun stopTimer() {
        timer.visibility = View.GONE
        timerRunnable?.let { timerHandler?.removeCallbacks(it) }
    }

    private fun takePhoto(arFragment: ArFrontFacingFragment) {
        val bitmap =
            Bitmap.createBitmap(
                arFragment.requireView().width,
                arFragment.requireView().height,
                Bitmap.Config.ARGB_8888,
            )
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()

        PixelCopy.request(arFragment.arSceneView, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap)
                    Handler(Looper.getMainLooper()).post {
                        StyleableToast.makeText(
                            requireContext(),
                            "Photo saved in gallery",
                            R.style.mytoast,
                        ).show()
                    }
                } catch (ex: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        StyleableToast.makeText(requireContext(), ex.toString(), R.style.mytoast)
                            .show()
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    StyleableToast.makeText(
                        requireContext(),
                        "Failed to save a photo",
                        R.style.mytoast,
                    ).show()
                }
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    private fun saveBitmapToDisk(bitmap: Bitmap) {
        val contentValues =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "AR_Image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/GlamARtist")
            }

        val uri =
            requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues,
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

package com.cvut.arfittingroom.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.model.element.impl.Gif
import com.cvut.arfittingroom.model.GIFS_COLLECTION
import com.cvut.arfittingroom.model.IMAGES_COLLECTION
import com.cvut.arfittingroom.model.LOOKS_COLLECTION
import com.cvut.arfittingroom.model.NUM_OF_ELEMENTS_IN_ROW_BIG_MENU
import com.cvut.arfittingroom.model.to.ImageTO
import com.cvut.arfittingroom.model.to.LookTO
import com.cvut.arfittingroom.module.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.github.muddz.styleabletoast.StyleableToast
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifDrawableBuilder
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

class ImagesMenuFragment(private val drawView: DrawView) : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_images_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.vertical_scroll_view).visibility = View.VISIBLE
        view.findViewById<GridLayout>(R.id.vertical_options).columnCount =
            NUM_OF_ELEMENTS_IN_ROW_BIG_MENU
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImage(it)
        } ?: run {
            StyleableToast.makeText(
                requireContext(),
                "Failed to select image.",
                R.style.mytoast
            ).show()
        }
    }

    fun fetchImages() {
        val options = requireView().findViewById<GridLayout>(R.id.vertical_options)
        options.removeAllViews()
        firestore.collection(IMAGES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val images = result.documents.mapNotNull { it.toObject(ImageTO::class.java) }
                updateImagesMenu(images)
            }
            .addOnFailureListener { ex ->
                StyleableToast.makeText(
                    requireContext(),
                    ex.message,
                    Toast.LENGTH_SHORT,
                    R.style.mytoast,
                ).show()
            }
    }

    private fun addImage(imageTO: ImageTO) {
        if (imageTO.isAnimated) {
            downloadGif(imageTO.ref) { gif, ref -> drawView.addGif(gif, ref) }

        } else {
            downloadImage(imageTO.ref) { bitmap, imageRef ->
                drawView.addImage(bitmap, imageRef)
            }
        }
    }

    private fun updateImagesMenu(imagesTO: List<ImageTO>) {
        val options = requireView().findViewById<GridLayout>(R.id.vertical_options)
        options.post {
            val imageWidth =
                (options.width - options.paddingStart - options.paddingEnd) / NUM_OF_ELEMENTS_IN_ROW_BIG_MENU
            imagesTO.forEach { image ->
                val button =
                    ImageButton(context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        background = null
                        id = image.ref.hashCode()
                        setOnClickListener {
                            addImage(image)
                        }
                    }

                val params =
                    GridLayout.LayoutParams().apply {
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        height = imageWidth
                        width = imageWidth
                    }

                params.setGravity(Gravity.START)

                options.addView(button, params)

                if (image.isAnimated) {
                    GlideApp.with(this)
                        .asGif()
                        .load(storage.getReference(image.ref))
                        .into(button)
                } else {
                    GlideApp.with(this)
                        .load(storage.getReference(image.ref))
                        .thumbnail()
                        .into(button)
                }
            }
        }
    }

    fun uploadImage() {
        getImage.launch("image/*")
    }

    private fun uploadImage(fileUri: Uri) {
        StyleableToast.makeText(
            requireContext(),
            "Starting to upload",
            R.style.mytoast
        ).show()

        val file = File(fileUri.path!!)
        val fileSize = file.length() / (1024 * 1024)

        val isAnimated = when (val mimeType = requireContext().contentResolver.getType(fileUri)) {
            "image/png" -> false
            "image/gif" -> true
            else -> {
                StyleableToast.makeText(
                    requireContext(),
                    "Unsupported file format: $mimeType",
                    R.style.mytoast
                ).show()
                return
            }
        }

        if (fileSize > 30) {
            StyleableToast.makeText(
                requireContext(),
                "Image size should not exceed 30 MB",
                R.style.mytoast
            ).show()
            return
        }

        val collection = if (isAnimated) GIFS_COLLECTION else IMAGES_COLLECTION
        val ref = "$collection/${file.name}${Random.nextInt()}"
        val fileRef = storage.getReference(ref)

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                val image = ImageTO(ref = ref, isAnimated = isAnimated)
                firestore.collection(IMAGES_COLLECTION).document().set(image)
                fetchImages()
                StyleableToast.makeText(
                    requireContext(),
                    "Uploaded successfully",
                    R.style.mytoast
                ).show()
            }
            .addOnFailureListener { ex ->
                StyleableToast.makeText(
                    requireContext(),
                    "Failed to upload image: ${ex.message}",
                    R.style.mytoast
                ).show()
            }
    }


    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun downloadImage(
        imageRef: String,
        onComplete: () -> Unit = {},
        onDownload: (Bitmap, String) -> Unit
    ) {
        Glide.with(this)
            .asBitmap()
            .load(storage.getReference(imageRef))
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        onDownload(adjustImage(resource), imageRef)
                        onComplete()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        onComplete()
                    }

                },
            )
    }


    fun downloadGif(
        gifRef: String,
        onComplete: () -> Unit = {},
        onDownload: (GifDrawable, String) -> Unit
    ) {
        val localFile = File.createTempFile("tempGif", ".gif")

        storage.getReference(gifRef).getFile(localFile).addOnSuccessListener {
            try {
                val gif = GifDrawable(localFile)

                onDownload(adjustGif(gif, localFile), gifRef)

                if (localFile.exists()) {
                    localFile.delete()
                }
                onComplete()
            } catch (ex: IOException) {
                ex.printStackTrace()
                StyleableToast.makeText(
                    requireContext(),
                    "Error processing image, ${ex.message}",
                    R.style.mytoast
                ).show()
                onComplete()
            }
        }.addOnFailureListener { ex ->
            StyleableToast.makeText(
                requireContext(),
                "Error loading image, ${ex.message}",
                R.style.mytoast
            ).show()
            // Clean up the temporary file in case of failure
            if (localFile.exists()) {
                localFile.delete()
            }
            onComplete()
        }
    }

    fun adjustImage(bitmap: Bitmap): Bitmap {
        val targetWidth = drawView.width / 3
        val targetHeight = drawView.height / 3

        val ratio =
            (targetWidth.toFloat() / bitmap.width).coerceAtMost(targetHeight.toFloat() / bitmap.height)

        val width = (ratio * bitmap.width).toInt()
        val height = (ratio * bitmap.height).toInt()

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun adjustGif(gifDrawable: GifDrawable, file: File): GifDrawable {
        return GifDrawableBuilder().with(gifDrawable)
            .from(file)
            .sampleSize(
                calculateInSampleSize(
                    gifDrawable.currentFrame.width,
                    gifDrawable.currentFrame.height,
                    drawView.width / 3,
                    drawView.height / 3,
                ),
            )
            .build()
    }

}
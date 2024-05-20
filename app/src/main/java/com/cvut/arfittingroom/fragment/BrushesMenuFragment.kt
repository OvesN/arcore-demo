package com.cvut.arfittingroom.fragment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.model.enums.EEditorMode
import com.cvut.arfittingroom.model.BRUSHES_COLLECTION
import com.cvut.arfittingroom.model.to.BrushTO
import com.cvut.arfittingroom.utils.ScreenUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import io.github.muddz.styleabletoast.StyleableToast

/**
 * Brushes menu fragment
 *
 * @property drawView
 */
class BrushesMenuFragment(private val drawView: DrawView) : Fragment() {
    private val brushesOptions = mutableListOf<BrushTO>()
    private var selectedViewId = 0
    private var underscoreSelectedView: View? = null
    private var editorModeChangeListener: EditorModeChangeListener? = null
    private var isInitialized = false
    private val paint =
        Paint().apply {
            strokeWidth = 10f
            color = Color.WHITE
        }
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        fetchBrushes(view)
    }

    fun changeColor(newColor: Int) {
        paint.color = newColor

        requireView().findViewById<ImageButton>(selectedViewId)?.let {
            it.imageTintList =
                ColorStateList.valueOf(newColor)
        }
    }

    private fun fetchBrushes(view: View) {
        firestore.collection(BRUSHES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                brushesOptions.clear()

                for (document in result) {
                    brushesOptions.add(document.toObject<BrushTO>().apply { id = document.id })
                }

                if (isAdded) {
                    updateBrushesMenu(view)
                }
            }
            .addOnFailureListener { ex ->
                if (isAdded) {
                    StyleableToast.makeText(requireContext(), ex.message, R.style.mytoast).show()
                }
            }
    }

    private fun updateBrushesMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
        options.removeAllViews()

        brushesOptions.forEach { brush ->
            val verticalContainer = createVerticalContainer()
            val underscoreLine = createUnderscoreLine()
            val imageButton = createImageButton(brush, underscoreLine)

            drawBrushPreview(brush, imageButton)
            setupInitialSelection(brush, imageButton, underscoreLine)

            verticalContainer.addView(imageButton)
            verticalContainer.addView(underscoreLine)
            options.addView(verticalContainer)
        }
    }

    private fun createVerticalContainer(): LinearLayout = LinearLayout(requireContext()).apply {
        layoutParams =
            LinearLayout.LayoutParams(
                ScreenUtil.dpToPx(50, requireContext()),
                ScreenUtil.dpToPx(50, requireContext()),
            )
        orientation = LinearLayout.VERTICAL
        background = null
        setPadding(ScreenUtil.dpToPx(5, requireContext()), 0, ScreenUtil.dpToPx(5, requireContext()), 0)
    }

    private fun createUnderscoreLine(): View = View(requireContext()).apply {
        layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5,
            )
        setBackgroundColor(Color.WHITE)
        visibility = View.INVISIBLE
    }

    private fun createImageButton(
        brush: BrushTO,
        underscoreLine: View,
    ): ImageButton {
        val imageSizePx = ScreenUtil.dpToPx(40, requireContext())
        return ImageButton(context).apply {
            id = brush.id.hashCode()
            layoutParams = ViewGroup.LayoutParams(imageSizePx, imageSizePx)
            ImageView.ScaleType.FIT_CENTER
            background = null
            setOnClickListener {
                selectBrush(brush, this, underscoreLine)
            }
        }
    }

    private fun drawBrushPreview(
        brush: BrushTO,
        imageButton: ImageButton,
    ) {
        val imageSizePx = ScreenUtil.dpToPx(40, requireContext())
        val bitmap = Bitmap.createBitmap(imageSizePx, imageSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = createPaint(brush)

        val path =
            Path().apply {
                val coord = imageSizePx.toFloat() / 2
                moveTo(coord, coord)
                lineTo(coord, coord + 2)
                lineTo(coord + 1, coord + 2)
                lineTo(coord + 1, coord)
            }

        if (brush.strokeTextureRef.isEmpty()) {
            canvas.drawPath(path, paint)
            imageButton.setImageBitmap(bitmap)
        } else {
            loadBrushTexture(brush, imageButton, imageSizePx)
        }
    }

    private fun createPaint(brush: BrushTO): Paint = Paint().apply {
        color = Color.WHITE
        strokeCap = brush.strokeCap
        strokeJoin = brush.strokeJoint
        strokeWidth = 70f
        isAntiAlias = true
        style = Paint.Style.STROKE
        if (brush.blurRadius != 0f) {
            maskFilter = BlurMaskFilter(brush.blurRadius, brush.blurType)
        }
    }

    private fun loadBrushTexture(
        brush: BrushTO,
        imageButton: ImageButton,
        imageSizePx: Int,
    ) {
        Glide.with(this)
            .asBitmap()
            .load(storage.getReference(brush.strokeTextureRef))
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        val scale =
                            minOf(
                                imageSizePx.toFloat() / resource.width,
                                imageSizePx.toFloat() / resource.height,
                            )
                        val scaledBitmap =
                            Bitmap.createScaledBitmap(
                                resource,
                                (resource.width * scale).toInt(),
                                (resource.height * scale).toInt(),
                                true,
                            )
                        imageButton.setImageBitmap(scaledBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                },
            )
    }

    private fun setupInitialSelection(
        brush: BrushTO,
        imageButton: ImageButton,
        underscoreLine: View,
    ) {
        val paint = createPaint(brush)
        if (!isInitialized) {
            underscoreSelectedView = underscoreLine
            selectedViewId = imageButton.id
            isInitialized = true
        }

        if (selectedViewId == imageButton.id) {
            underscoreSelectedView = underscoreLine
            underscoreLine.visibility = View.VISIBLE
            imageButton.imageTintList = ColorStateList.valueOf(paint.color)
        }
    }

    private fun selectBrush(
        brush: BrushTO,
        view: ImageButton,
        underscore: View,
    ) {
        underscoreSelectedView?.let { it.visibility = View.INVISIBLE }
        requireView().findViewById<ImageButton>(selectedViewId)?.let {
            it.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }

        if (selectedViewId == view.id) {
            selectedViewId = 0
            underscore.visibility = View.GONE
            drawView.setEditingMode()
        } else {
            editorModeChangeListener?.onEditingModeExit(EEditorMode.BRUSH)

            selectedViewId = view.id
            underscoreSelectedView = underscore
            underscore.visibility = View.VISIBLE

            view.imageTintList = ColorStateList.valueOf(paint.color)

            if (brush.strokeTextureRef.isNotEmpty()) {
                Glide.with(this)
                    .asBitmap()
                    .load(storage.getReference(brush.strokeTextureRef))
                    .into(
                        object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?,
                            ) {
                                drawView.setBrush(brush, resource)
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                StyleableToast.makeText(requireContext(), "Error loading brush", R.style.mytoast).show()
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        },
                    )
            } else {
                drawView.setBrush(brush)
            }
        }
    }

    fun checkIfBrushSelected() {
        if (selectedViewId == 0 && isInitialized) {
            drawView.setEditingMode()
        } else {
            editorModeChangeListener?.onEditingModeExit(newMode = EEditorMode.BRUSH)
        }
    }

    fun setEditorStateChangeListener(listener: EditorModeChangeListener) {
        editorModeChangeListener = listener
    }
}

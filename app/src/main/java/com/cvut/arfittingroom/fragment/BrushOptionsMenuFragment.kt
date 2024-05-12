package com.cvut.arfittingroom.fragment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.model.BRUSHES_COLLECTION
import com.cvut.arfittingroom.model.to.BrushTO
import com.cvut.arfittingroom.module.GlideApp
import com.cvut.arfittingroom.utils.ScreenUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import io.github.muddz.styleabletoast.StyleableToast

class BrushOptionsMenuFragment(private val drawView: DrawView) : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val brushesOptions = mutableListOf<BrushTO>()
    private var selectedViewId = 0
    private var underscoreSelectedView: View? = null
    private var selectedColor: Int = Color.BLACK

    private var isInitialized = false

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchBrushes(view)

    }

    fun changeColor(newColor: Int) {
        selectedColor = newColor

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
                    brushesOptions.add(document.toObject())
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

        for (brush in brushesOptions) {

            val verticalContainer = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
            }

            val underscoreLine = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
                )
                setBackgroundColor(Color.WHITE)
                visibility = View.GONE
            }

            val imageButton =
                ImageButton(context).apply {
                    id = brush.imagePreviewRef.hashCode()
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ScreenUtil.dpToPx(30, requireContext()),
                            ScreenUtil.dpToPx(30, requireContext()),
                        )
                    background = null
                    setOnClickListener {
                        selectBrush(brush, this, underscoreLine)
                        underscoreLine.visibility =
                            if (underscoreLine.visibility == View.GONE) View.VISIBLE else View.GONE
                    }
                }

            verticalContainer.addView(imageButton)
            verticalContainer.addView(underscoreLine)
            options.addView(verticalContainer)

            if (!isInitialized) {
                imageButton.callOnClick()
                isInitialized = true
            }

            GlideApp.with(this)
                .load(storage.getReference(brush.imagePreviewRef))
                .thumbnail()
                .into(imageButton)
        }
    }

    private fun selectBrush(brush: BrushTO, view: ImageButton, underscore: View) {
        underscoreSelectedView?.let { it.visibility = View.GONE }
        view.imageTintList = ColorStateList.valueOf(selectedColor)

        if (selectedViewId == view.id) {
            selectedViewId = 0
            drawView.setEditingMode()
            return
        }

        selectedViewId = brush.imagePreviewRef.hashCode()
        underscoreSelectedView = underscore

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
                        drawView.setBrush(brush)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                },
            )
    }

}


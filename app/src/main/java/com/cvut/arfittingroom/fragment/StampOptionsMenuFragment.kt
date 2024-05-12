package com.cvut.arfittingroom.fragment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.utils.ScreenUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class StampOptionsMenuFragment(private val strategies: Map<String, @JvmSuppressWildcards PathCreationStrategy>, private val drawView: DrawView):Fragment() {
    private var selectedViewId = 0
    private var selectedColor: Int = Color.BLACK
    private var underscoreSelectedView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createStampMenu(view)
    }

    private fun createStampMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
        options.removeAllViews()

        val imageSizePx = ScreenUtil.dpToPx(30, requireContext())

        for (stamp in strategies) {
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

            //TODO stoke or fill?
            val bitmap = Bitmap.createBitmap(imageSizePx, imageSizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            val path = stamp.value.createPath(imageSizePx / 2f, imageSizePx / 2f, imageSizePx / 2f)
            canvas.drawPath(path, paint)

            val imageButton = ImageButton(context).apply {
                id = stamp.key.hashCode()
                layoutParams = ViewGroup.LayoutParams(imageSizePx, imageSizePx)
                setImageBitmap(bitmap)
                background = null
                setOnClickListener {
                    selectStamp(stamp.value, this, underscoreLine)
                    underscoreLine.visibility = if (underscoreLine.visibility == View.GONE) View.VISIBLE else View.GONE
                }
            }

            verticalContainer.addView(imageButton)
            verticalContainer.addView(underscoreLine)
            options.addView(verticalContainer)

        }
    }

    private fun selectStamp(pathCreationStrategy: PathCreationStrategy, view: ImageView, underscore: View) {
        underscoreSelectedView?.let { it.visibility = View.GONE }
        view.imageTintList = ColorStateList.valueOf(selectedColor)

        if (selectedViewId == view.id) {
            selectedViewId = 0
            drawView.setEditingMode()
            return
        }

        selectedViewId = view.id
        underscoreSelectedView = underscore

        drawView.setStamp(pathCreationStrategy)
    }


    fun changeColor(newColor: Int) {
        selectedColor = newColor
        requireView().findViewById<ImageButton>(selectedViewId).imageTintList = ColorStateList.valueOf(newColor)
    }

}
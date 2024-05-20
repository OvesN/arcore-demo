package com.cvut.arfittingroom.fragment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
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

class StampsMenuFragment(
    private val strategies: Map<String, @JvmSuppressWildcards PathCreationStrategy>,
    private val drawView: DrawView,
) : Fragment() {
    private var selectedViewId = 0
    private val paint =
        Paint().apply {
            strokeWidth = 6f
            color = Color.WHITE
            style = Style.FILL
        }
    private var underscoreSelectedView: View? = null
    private var editorStateChangeListener: EditorStateChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        createStampMenu(view)
    }

    private fun createStampMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
        options.removeAllViews()

        val imageSizePx = ScreenUtil.dpToPx(40, requireContext())

        for (stamp in strategies) {
            val verticalContainer =
                LinearLayout(requireContext()).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(
                            ScreenUtil.dpToPx(50, requireContext()),
                            ScreenUtil.dpToPx(50, requireContext()),
                        )
                    orientation = LinearLayout.VERTICAL
                    background = null
                }

            verticalContainer.setPadding(ScreenUtil.dpToPx(5, requireContext()), 0, ScreenUtil.dpToPx(5, requireContext()), 0)
            val underscoreLine =
                View(requireContext()).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            5,
                        )
                    setBackgroundColor(Color.WHITE)
                    visibility = View.INVISIBLE
                }

            val bitmap = Bitmap.createBitmap(imageSizePx, imageSizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint =
                Paint().apply {
                    color = Color.WHITE
                    style = paint.style
                    strokeWidth = 3f
                    isAntiAlias = true
                }
            val path = stamp.value.createPath(imageSizePx / 2f, imageSizePx / 2f, imageSizePx / 2f)
            canvas.drawPath(path, paint)

            val imageButton =
                ImageButton(context).apply {
                    id = stamp.key.hashCode()
                    layoutParams = ViewGroup.LayoutParams(imageSizePx, imageSizePx)
                    setImageBitmap(bitmap)
                    ImageView.ScaleType.FIT_CENTER
                    background = null
                    setOnClickListener {
                        selectStamp(stamp.value, this, underscoreLine)
                    }
                }

            if (selectedViewId == imageButton.id) {
                underscoreSelectedView = underscoreLine
                underscoreLine.visibility = View.VISIBLE
                imageButton.imageTintList = ColorStateList.valueOf(paint.color)
            }

            verticalContainer.addView(imageButton)
            verticalContainer.addView(underscoreLine)
            options.addView(verticalContainer)
        }
    }

    private fun selectStamp(
        pathCreationStrategy: PathCreationStrategy,
        view: ImageView,
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
            editorStateChangeListener?.onEditingStateExit()

            selectedViewId = view.id
            underscoreSelectedView = underscore
            underscore.visibility = View.VISIBLE

            view.imageTintList = ColorStateList.valueOf(paint.color)
            drawView.setStamp(pathCreationStrategy)
        }
    }

    fun changeColor(
        newColor: Int,
        fill: Boolean,
    ) {
        paint.color = newColor

        paint.style = if (fill) Style.FILL else Style.STROKE

        createStampMenu(requireView())
    }

    fun checkIfStampSelected() {
        if (selectedViewId != 0) {
            drawView.setStampMode()
        } else {
            drawView.setEditingMode()
        }
    }

    fun setEditorStateChangeListener(listener: EditorStateChangeListener) {
        editorStateChangeListener = listener
    }
}

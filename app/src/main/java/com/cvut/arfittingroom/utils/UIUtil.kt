package com.cvut.arfittingroom.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.cvut.arfittingroom.R
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

object UIUtil {
    fun createDivider(context: Context): View =
        View(context).apply {
            layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
            background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
        }

    fun showColorPickerDialog(
        context: Context,
        shouldShowFillOption: Boolean = false,
        onColorSelected: (Int) -> Unit,
    ) {
        ColorPickerDialog.Builder(context)
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(
                R.string.OK,
                ColorEnvelopeListener { envelope, _ ->
                    onColorSelected(envelope.color)
                },
            )
            .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            //.setSingleChoiceItems()
            //.setView()
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    fun selectMakeupButton(view: View) {
        val layers =
            arrayOf(
                ContextCompat.getDrawable(view.context, R.drawable.head_model)!!,
                ContextCompat.getDrawable(view.context, R.drawable.border)!!,
            )

        view.background = LayerDrawable(layers)
    }

    fun deselectMakeupOptionButton(view: View) {
        view.background = ContextCompat.getDrawable(view.context, R.drawable.head_model)!!
    }

    fun selectSquareButton(view: View) {
        view.background = ContextCompat.getDrawable(view.context, R.drawable.border)!!
    }

    fun deselectButton(view: View) {
        view.background = null
    }

    fun selectColorButton(view: View) {
        val layers =
            arrayOf(
                createColorOptionImage(view.context, view.id),
                ContextCompat.getDrawable(view.context, R.drawable.circle_border)!!,
            )

        view.background = LayerDrawable(layers)
    }

    fun deselectColorButton(view: View) {
        view.background = createColorOptionImage(view.context, view.id)
    }

    fun createColorOptionImage(
        context: Context,
        color: Int,
    ): Drawable {
        val icon = ContextCompat.getDrawable(context, R.drawable.circle)!!.mutate()
        val wrap = DrawableCompat.wrap(icon)
        DrawableCompat.setTint(wrap, color)

        return wrap
    }

    fun animateButton(view: View) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.5f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.5f)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f, 1.0f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f, 1.0f)

        scaleUpX.duration = 150
        scaleUpY.duration = 150
        scaleDownX.duration = 150
        scaleDownY.duration = 150

        val scaleUp =
            AnimatorSet().apply {
                play(scaleUpX).with(scaleUpY)
                interpolator = AccelerateDecelerateInterpolator()
            }

        val scaleDown =
            AnimatorSet().apply {
                play(scaleDownX).with(scaleDownY)
                interpolator = AccelerateDecelerateInterpolator()
            }

        AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            start()
        }
    }
}

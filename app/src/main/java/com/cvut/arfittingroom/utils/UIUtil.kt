package com.cvut.arfittingroom.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import com.cvut.arfittingroom.R
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.util.function.Function

object UIUtil {
    fun createDivider(context: Context): View = View(context).apply {
        layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
        background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
    }

    fun createTextButton(context: Context, text: String, listener: View.OnClickListener): Button =
        Button(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            this.text = text
            isAllCaps = false
            background = null
            setOnClickListener(listener)
        }

    fun createImageButton() {}


    fun showColorPickerDialog(
        context: Context,
        onColorSelected: (Int) -> Unit
    ) {
        ColorPickerDialog.Builder(context)
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(
                R.string.OK,
                ColorEnvelopeListener { envelope, _ ->
                    onColorSelected(envelope.color)
                }
            )
            .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }


}

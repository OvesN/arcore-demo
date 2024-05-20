package com.cvut.arfittingroom.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.model.to.LookTO
import com.skydoves.colorpickerview.ColorPickerView

/**
 * U i util
 *
 * @author Veronika Ovsyannikova
 */
object UIUtil {
    fun createDivider(context: Context): View =
        View(context).apply {
            layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
            background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
        }

    fun showLookInfoPopup(
        context: Context,
        lookTO: LookTO,
        isAuthor: Boolean,
        view: View,
        onLookDelete: () -> Unit = {},
        onChangeIsPublic: (Boolean) -> Unit,
    ) {
        val popupView: View = LayoutInflater.from(context).inflate(R.layout.popup_look_info, null)

        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        val checkBox = popupView.findViewById<CheckBox>(R.id.is_public_checkbox).apply {
            isChecked = lookTO.isPublic
        }

        val authorNameText = popupView.findViewById<TextView>(R.id.look_author_name).apply {
            text = if (isAuthor) "${lookTO.author} (you)" else lookTO.author
        }

        val cancelButton: Button = popupView.findViewById(R.id.cancel_popup_button)
        val okButton: Button = popupView.findViewById(R.id.ok_popup_button)
        val deleteButton: Button = popupView.findViewById(R.id.delete_look_button)

        cancelButton.setOnClickListener {
            popupWindow.dismiss()
        }

        if (isAuthor) {
            deleteButton.visibility = View.VISIBLE
            checkBox.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                showDeleteLookWarning(context, onLookDelete)
                popupWindow.dismiss()
            }

            okButton.visibility = View.VISIBLE
            okButton.setOnClickListener {
                onChangeIsPublic(checkBox.isChecked)
                popupWindow.dismiss()
            }
        }

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private fun showDeleteLookWarning(
        context: Context,
        onLookDelete: () -> Unit = {},
    ) {
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.popup_really_delete, null)

        val dialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

        dialogView.findViewById<Button>(R.id.cancel_popup_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.delete_button).setOnClickListener {
            onLookDelete()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showDeleteLayerDialog(
        context: Context,
        onDelete: () -> Unit,
    ) {
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.popup_delete_layer, null)
        val dialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

        dialogView.findViewById<Button>(R.id.cancel_popup_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.delete_button).setOnClickListener {
            onDelete()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showMoveToLayerDialog(
        context: Context,
        currentLayerIndex: Int,
        maxLayerIndex: Int,
        onLayerSelected: (Int) -> Unit,
    ) {
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.popup_move_to_layer, null)
        val dialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

        val layerSpinner: Spinner = dialogView.findViewById(R.id.layer_spinner)
        val layers = (0..maxLayerIndex).toList()
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, layers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        layerSpinner.adapter = adapter

        layerSpinner.setSelection(currentLayerIndex)

        dialogView.findViewById<Button>(R.id.cancel_popup_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.move_to_layer_button).setOnClickListener {
            val selectedLayer = layerSpinner.selectedItem as Int
            onLayerSelected(selectedLayer)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showColorPickerDialog(
        context: Context,
        initialColor: Int,
        fill: Boolean = false,
        shouldShowFillCheckbox: Boolean = false,
        shouldShowPipette: Boolean = false,
        onPipetteSelected: () -> Unit = {},
        onColorSelected: (Int, Boolean) -> Unit,
    ) {
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null)

        val colorPickerView = dialogView.findViewById<ColorPickerView>(R.id.colorPickerView)

        val checkbox = dialogView.findViewById<CheckBox>(R.id.fill_checkbox)
        val pipette = dialogView.findViewById<View>(R.id.pipette_button)
        checkbox.isChecked = fill

        colorPickerView.attachAlphaSlider(dialogView.findViewById(R.id.alphaSlideBar))
        colorPickerView.attachBrightnessSlider(dialogView.findViewById(R.id.brightnessSlideBar))
        colorPickerView.setInitialColor(initialColor)
        colorPickerView.preferenceName = "MyColorPicker"

        val dialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

        dialogView.findViewById<Button>(R.id.cancel_popup_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.ok_button).setOnClickListener {
            onColorSelected(colorPickerView.colorEnvelope.color, checkbox.isChecked)
            dialog.dismiss()
        }

        if (shouldShowFillCheckbox) {
            checkbox?.let { it.visibility = View.VISIBLE }
        }
        if (shouldShowPipette) {
            pipette?.let { it.visibility = View.VISIBLE }
            pipette.setOnClickListener {
                onPipetteSelected()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    fun showClearAllDialog(
        context: Context,
        onClearAll: () -> Unit,
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.popup_clear_all, null)

        val dialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

        dialogView.findViewById<Button>(R.id.cancel_popup_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.discard_button).setOnClickListener {
            onClearAll()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun selectLookButton(view: View) {
        if (view is ImageButton) {
            selectHeadBackgroundButton(view)
        } else {
            selectSquareButton(view)
        }
    }

    fun deselectLookButton(view: View) {
        if (view is ImageButton) {
            deselectHeadBackgroundButton(view)
        } else {
            deselectButton(view)
        }
    }

    fun selectHeadBackgroundButton(view: View) {
        val layers =
            arrayOf(
                ContextCompat.getDrawable(view.context, R.drawable.head_model)!!,
                ContextCompat.getDrawable(view.context, R.drawable.border)!!,
            )

        view.background = LayerDrawable(layers)
    }

    fun deselectHeadBackgroundButton(view: View) {
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

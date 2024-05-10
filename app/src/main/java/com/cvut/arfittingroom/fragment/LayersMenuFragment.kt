package com.cvut.arfittingroom.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.model.enums.ELayerEditAction
import com.cvut.arfittingroom.draw.service.LayerManager
import com.cvut.arfittingroom.utils.ScreenUtil
import com.cvut.arfittingroom.utils.ScreenUtil.dpToPx

/**
 * Layers menu fragment, manages menu for layers
 *
 * @property drawView
 */
class LayersMenuFragment(private val drawView: DrawView) : Fragment() {
    private lateinit var layersButtonsContainer: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_layers_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layersButtonsContainer = view.findViewById(R.id.layers_buttons_container)
        view.findViewById<ImageButton>(R.id.add_layer_button).setOnClickListener {
            updateLayersButtons(drawView.addLayer() + 1)
        }
    }

    /**
     * Create buttons for layers in reverse order and select the active one
     *
     * @param numOfLayers
     */
    fun updateLayersButtons(numOfLayers: Int) {
        layersButtonsContainer.removeAllViews()

        for (i in numOfLayers - 1 downTo 0) {
            val button =
                Button(requireContext()).apply {
                    height = dpToPx(30, requireContext())
                    setTypeface(typeface, Typeface.BOLD)
                    text = i.toString()
                    setOnClickListener { showLayerEditDialog(i, this) }
                }

            if (i == drawView.layerManager.getActiveLayerIndex()) {
                button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorActive
                    )
                )
            } else {
                button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTransparent
                    )
                )
            }

            layersButtonsContainer.addView(button, layersButtonsContainer.childCount)
        }
    }

    private fun showLayerEditDialog(
        layerIndex: Int,
        button: Button,
    ) {
        val options = ELayerEditAction.entries.map { it.string }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Edit layer $layerIndex")
            .setItems(options) { _, which ->

                when (options[which]) {
                    ELayerEditAction.DELETE.string -> drawView.removeLayer(layerIndex)

                    ELayerEditAction.MOVE_DOWN.string ->
                        drawView.moveLayer(
                            layerIndex,
                            layerIndex - 1,
                        )

                    ELayerEditAction.MOVE_UP.string ->
                        drawView.moveLayer(
                            layerIndex,
                            layerIndex + 1,
                        )

                    ELayerEditAction.SELECT.string -> drawView.setActiveLayer(layerIndex)
                }

                updateLayersButtons(drawView.layerManager.getNumOfLayers())
            }
            .show()
    }
}
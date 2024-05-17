package com.cvut.arfittingroom.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.utils.ScreenUtil.dpToPx
import com.cvut.arfittingroom.utils.UIUtil
import io.github.muddz.styleabletoast.StyleableToast

/**
 * Layers menu fragment, manages menu for layers
 *
 * @property drawView
 */
class LayersMenuFragment(private val drawView: DrawView) : Fragment() {
    private lateinit var layersButtonsContainer: LinearLayout
    private lateinit var layerUpButton: ImageButton
    private lateinit var layerDownButton: ImageButton
    private lateinit var isVisibleButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_layers_menu, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        layersButtonsContainer = view.findViewById(R.id.layers_buttons_container)

        view.findViewById<ImageButton>(R.id.add_layer_button).setOnClickListener {

            updateLayersButtons(drawView.addLayer() + 1)
            drawView.layerManager.makeLayersSemiTransparentExceptOne(drawView.layerManager.getActiveLayerIndex())
            drawView.invalidate()

            StyleableToast.makeText(requireContext(), "New layer added", R.style.mytoast).show()
        }

        view.findViewById<ImageButton>(R.id.delete_layer_button).setOnClickListener {
            UIUtil.showDeleteLayerDialog(requireContext()) {
                drawView.removeLayer(drawView.layerManager.getActiveLayerIndex())
                updateLayersButtons(drawView.layerManager.getNumOfLayers())
                StyleableToast.makeText(requireContext(), "Layer deleted", R.style.mytoast).show()
            }
        }

        // This will block the touch event so it will not propagate to draw view
        view.findViewById<LinearLayout>(R.id.layers_menu_layout).setOnTouchListener { v, event ->
            true
        }

        layerUpButton = view.findViewById(R.id.move_layer_up_button)
        layerUpButton.setOnClickListener {
            val layerIndex = drawView.layerManager.getActiveLayerIndex()
            drawView.moveLayer(
                layerIndex,
                layerIndex + 1,
            )
            updateLayersButtons(drawView.layerManager.getNumOfLayers())
            StyleableToast.makeText(requireContext(), "Layer moved up", R.style.mytoast).show()
        }

        layerDownButton = view.findViewById(R.id.move_layer_down_button)
        layerDownButton.setOnClickListener {
            val layerIndex = drawView.layerManager.getActiveLayerIndex()
            drawView.moveLayer(
                layerIndex,
                layerIndex - 1,
            )
            updateLayersButtons(drawView.layerManager.getNumOfLayers())
            StyleableToast.makeText(requireContext(), "Layer moved down", R.style.mytoast).show()
        }

        isVisibleButton = view.findViewById(R.id.is_visible_button)
        isVisibleButton.setOnClickListener {
            val layerIndex = drawView.layerManager.getActiveLayerIndex()
            drawView.toggleLayerVisibility(layerIndex)

            setIsVisibleButton(layerIndex)

            if (drawView.layerManager.isVisible(layerIndex)) {
                StyleableToast.makeText(requireContext(), "Layer is visible now", R.style.mytoast)
                    .show()
            } else {
                StyleableToast.makeText(requireContext(), "Layer is invisible now", R.style.mytoast)
                    .show()
            }
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
                    setOnClickListener {
                        drawView.layerManager.setActiveLayer(i)
                        drawView.layerManager.makeLayersSemiTransparentExceptOne(i)
                        updateLayersButtons(drawView.layerManager.getNumOfLayers())

                        drawView.invalidate()
                    }
                }

            if (i == drawView.layerManager.getActiveLayerIndex()) {
                setIsVisibleButton(i)
                button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorActive,
                    ),
                )
            } else {
                button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTransparent,
                    ),
                )
            }

            layersButtonsContainer.addView(button, layersButtonsContainer.childCount)
        }
    }

    private fun setIsVisibleButton(layerIndex: Int) {
        if (drawView.layerManager.isVisible(layerIndex)) {
            isVisibleButton.background =
                (ContextCompat.getDrawable(requireContext(), R.drawable.visible_layer))
        } else {
            isVisibleButton.background =
                (ContextCompat.getDrawable(requireContext(), R.drawable.invisible_layer))
        }
    }
}

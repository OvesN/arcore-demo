package com.cvut.arfittingroom.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.UIChangeListener
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.model.enums.ELayerEditAction
import com.cvut.arfittingroom.draw.model.enums.EShape
import com.cvut.arfittingroom.utils.UIUtil
import com.google.android.material.slider.Slider


private const val DEFAULT_COLOR = Color.TRANSPARENT
private val SELECTED_COLOR = Color.parseColor("#FF5722")

class MakeupEditorFragment : Fragment() {
    var backgroundBitmap: Bitmap? = null
    private lateinit var drawView: DrawView
    private lateinit var imageView: ImageView
    private lateinit var slider: Slider
    private lateinit var layersButtonsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_makeup_editor, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawView = view.findViewById(R.id.draw_view)
        drawView.applyBitmapBackground(backgroundBitmap)
        imageView = view.findViewById(R.id.face_image)
        slider = view.findViewById(R.id.stroke_size_slider)
        layersButtonsContainer = view.findViewById(R.id.dynamic_layer_buttons_container)

        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            drawView.saveBitmap {
                showMainLayout()
            }
        }

        view.findViewById<ImageButton>(R.id.button_color_picker).setOnClickListener {
            UIUtil.showColorPickerDialog(requireContext()) { envelopColor ->
                drawView.setColor(
                    envelopColor,
                )
            }
        }
        view.findViewById<ImageButton>(R.id.button_redo).setOnClickListener {
            drawView.redo()
        }
        view.findViewById<ImageButton>(R.id.button_undo).setOnClickListener {
            drawView.undo()
        }
        view.findViewById<ImageButton>(R.id.button_star).setOnClickListener {
            toggleStrokeShape(EShape.STAR)
        }
        view.findViewById<ImageButton>(R.id.button_heart).setOnClickListener {
            toggleStrokeShape(EShape.HEART)
        }
        view.findViewById<ImageButton>(R.id.button_flowers_image).setOnClickListener {
            addImage(R.drawable.flowers)
        }
        view.findViewById<ImageButton>(R.id.button_star).setOnClickListener {
            toggleStrokeShape(EShape.STAR)
        }
        view.findViewById<ImageButton>(R.id.button_heart).setOnClickListener {
            toggleStrokeShape(EShape.HEART)
        }

        view.findViewById<ImageButton>(R.id.button_gif).setOnClickListener {
            addGif(R.drawable.donut)
        }
        view.findViewById<ImageButton>(R.id.button_gif2).setOnClickListener {
            addGif(R.drawable.hamburger)
        }
        slider.addOnChangeListener { _, value, _ ->
            drawView.setStrokeWidth(value)
        }
        drawView.setStrokeWidth(slider.value)

        // TODO fix num or indexes or whut
        view.findViewById<ImageButton>(R.id.button_add_layer).setOnClickListener {
            updateLayersButtons(drawView.addLayer() + 1)
        }

        drawView.setOnLayerInitializedListener(
            object : DrawView.OnLayerInitializedListener {
                override fun onLayerInitialized(numOfLayers: Int) {
                    updateLayersButtons(numOfLayers)
                }
            },
        )

        drawView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                drawView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                if (drawView.layerManager.getNumOfLayers() == 0) {
                    drawView.layerManager.addLayer(drawView.width, drawView.height)
                    drawView.layerInitializedListener?.onLayerInitialized(drawView.layerManager.getNumOfLayers())
                }
            }
        })

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireContext().applicationContext as ARFittingRoomApplication).appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()

        drawView.layerManager.resetAllGifs()
        drawView.layerManager.setAllGifsToStaticMode()

        updateLayersButtons(drawView.layerManager.getNumOfLayers())
    }

    override fun onPause() {
        super.onPause()
        drawView.stopAnimation()
    }

    // Create buttons for layers in reverse order and select the active one
    private fun updateLayersButtons(numOfLayers: Int) {
        layersButtonsContainer.removeAllViews()

        for (i in numOfLayers - 1 downTo 0) {
            val button =
                Button(requireContext()).apply {
                    text = i.toString()
                    setOnClickListener { showLayerEditDialog(i, this) }
                }

            if (i == drawView.layerManager.getActiveLayerIndex()) {
                button.setBackgroundColor(SELECTED_COLOR)
            } else {
                button.setBackgroundColor(DEFAULT_COLOR)
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

                // updateLayersButtons(drawView.layerManager.getNumOfLayers())
            }
            .show()
    }

    private fun toggleStrokeShape(shape: EShape) {
        drawView.layerManager.deselectAllElements()
        drawView.strokeShape = if (drawView.strokeShape == shape) EShape.NONE else shape
        drawView.selectedElement = null
    }

    private fun addImage(imageId: Int) {
        drawView.loadImage(imageId)
    }

    private fun addGif(gifId: Int) {
        drawView.loadGif(gifId)
    }


    private fun showMainLayout() {
        val listener = context as? UIChangeListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }
        listener.showMainLayout()
    }

    fun clearAll() {
        if (::drawView.isInitialized) {
            drawView.clearCanvas()
        }
    }


    fun applyBackgroundBitmap(bitmap: Bitmap?) {
        backgroundBitmap = bitmap
        if (::drawView.isInitialized) {
            drawView.applyBitmapBackground(backgroundBitmap)
        }
    }

    companion object {
        const val MAKEUP_EDITOR_FRAGMENT_TAG ="MakeupEditorFragmentTag"
    }

}
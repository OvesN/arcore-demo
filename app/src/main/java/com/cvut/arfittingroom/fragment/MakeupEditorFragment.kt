package com.cvut.arfittingroom.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.UIChangeListener
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.Layer
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.model.enums.ELayerEditAction
import com.cvut.arfittingroom.draw.model.enums.EShape
import com.cvut.arfittingroom.model.TRANSPARENT_CODE
import com.cvut.arfittingroom.model.to.drawhistory.EditorStateTO
import com.cvut.arfittingroom.model.to.drawhistory.ElementTO
import com.cvut.arfittingroom.model.to.drawhistory.LayerTO
import com.cvut.arfittingroom.service.Mapper
import com.cvut.arfittingroom.utils.UIUtil
import com.google.android.material.slider.Slider
import com.lukelorusso.verticalseekbar.VerticalSeekBar
import java.util.LinkedList
import javax.inject.Inject

private const val DEFAULT_COLOR = Color.TRANSPARENT
private val SELECTED_COLOR = Color.parseColor("#FF5722")

class MakeupEditorFragment : Fragment() {
    private var backgroundBitmap: Bitmap? = null
    var editorStateTO: EditorStateTO? = null
    private lateinit var drawView: DrawView
    private lateinit var slider: VerticalSeekBar
    private lateinit var layersButtonsContainer: LinearLayout

    @Inject
    lateinit var strategies: Map<String, @JvmSuppressWildcards PathCreationStrategy>

    @Inject
    lateinit var mapper: Mapper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_makeup_editor, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        drawView = view.findViewById(R.id.draw_view)
        drawView.applyBitmapBackground(backgroundBitmap)
        slider = view.findViewById(R.id.stroke_size_slider)

        slider.thumbPlaceholderDrawable = ContextCompat.getDrawable(view.context, R.drawable.slider)
        slider.thumbContainerColor = Color.TRANSPARENT


            //layersButtonsContainer = view.findViewById(R.id.dynamic_layer_buttons_container)

        drawView.post {
            drawView.setDimensions(drawView.width, drawView.height)
            drawView.invalidate()
            if (drawView.layerManager.getNumOfLayers() == 0) {
                drawView.layerManager.addLayer(drawView.width, drawView.height)
                    //  drawView.layerInitializedListener?.onLayerInitialized(drawView.layerManager.getNumOfLayers())
            }
        }
//
//        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
//            drawView.saveBitmap {
//                showMainLayout()
//            }
//        }
//
//        view.findViewById<ImageButton>(R.id.button_color_picker).setOnClickListener {
//            UIUtil.showColorPickerDialog(
//                requireContext(),
//                drawView.paintOptions.color,
//            ) { envelopColor ->
//                drawView.setColor(
//                    envelopColor,
//                )
//            }
//        }
//        view.findViewById<ImageButton>(R.id.button_redo).setOnClickListener {
//            drawView.redo()
//        }
        view.findViewById<ImageButton>(R.id.button_undo).setOnClickListener {
            drawView.undo()
        }

        slider.setOnProgressChangeListener {
            drawView.setStrokeWidth(it)
        }

        drawView.setStrokeWidth(slider.progress)

//        // TODO fix num or indexes or whut
//        view.findViewById<ImageButton>(R.id.button_add_layer).setOnClickListener {
//            updateLayersButtons(drawView.addLayer() + 1)
//        }

        drawView.setOnLayerInitializedListener(
            object : DrawView.OnLayerInitializedListener {
                override fun onLayerInitialized(numOfLayers: Int) {
                   // updateLayersButtons(numOfLayers)
                }
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireContext().applicationContext as ARFittingRoomApplication).appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        editorStateTO?.let {
            deserializeEditorState(it)
            editorStateTO = null
        }
        drawView.layerManager.resetAllGifs()
        drawView.layerManager.setAllGifsToStaticMode()

        //updateLayersButtons(drawView.layerManager.getNumOfLayers())
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
        drawView.layerManager.setAllGifsToStaticMode()
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

    fun serializeEditorState(): EditorStateTO {
        mapper.setDimensions(drawView.width, drawView.height)

        val layers = drawView.layerManager.layers
        val elementsTO = mutableListOf<ElementTO>()
        val layersTO = mutableListOf<LayerTO>()

        layers.forEachIndexed { index, layer ->
            elementsTO.addAll(layer.elements.values.map { mapper.elementToElementTO(it) })
            layersTO.add(mapper.layerToLayerTO(layer, index))
        }

        return EditorStateTO(
            elements = elementsTO,
            layers = layersTO,
        )
    }

    private fun deserializeEditorState(editorStateTO: EditorStateTO) {
        mapper.setDimensions(drawView.width, drawView.height)

        val elementsMap =
            editorStateTO.elements.associateBy(
                keySelector = { it.id },
                valueTransform = { mapper.elementTOtoElement(it) },
            )

        val sortedLayers = editorStateTO.layers.sortedBy { it.index }
        val layersList: LinkedList<Layer> = LinkedList()

        val layersMap =
            sortedLayers.associateBy(
                keySelector = { it.id },
                valueTransform = { layerTO ->
                    val layer =
                        mapper.layerTOtoLayer(layerTO)
                    layersList.add(layer)
                    layerTO.elements.forEach {
                        elementsMap[it]?.let { it1 -> layer.addElement(it1) }
                    }
                    layer
                },
            )

        drawView.layerManager.deleteLayers()

        drawView.layerManager.layers.addAll(layersList)
    }

    companion object {
        const val MAKEUP_EDITOR_FRAGMENT_TAG = "MakeupEditorFragmentTag"
    }
}

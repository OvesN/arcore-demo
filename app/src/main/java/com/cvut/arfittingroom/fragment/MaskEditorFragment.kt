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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.UIChangeListener
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.Layer
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.model.to.drawhistory.EditorStateTO
import com.cvut.arfittingroom.model.to.drawhistory.ElementTO
import com.cvut.arfittingroom.model.to.drawhistory.LayerTO
import com.cvut.arfittingroom.service.Mapper
import com.cvut.arfittingroom.utils.UIUtil
import com.lukelorusso.verticalseekbar.VerticalSeekBar
import java.util.LinkedList
import javax.inject.Inject

class MaskEditorFragment : Fragment() {
    private var backgroundBitmap: Bitmap? = null
    var editorStateTO: EditorStateTO? = null
    private var isLayersMenuShown = false
    private lateinit var drawView: DrawView
    private lateinit var sliderLayout: LinearLayout
    private lateinit var slider: VerticalSeekBar
    private lateinit var layersButton: ImageButton

    private lateinit var menuButtons: LinearLayout
    private lateinit var layersMenuFragment: LayersMenuFragment
    private lateinit var stampOptionsMenuFragment: StampOptionsMenuFragment
    private lateinit var imageMenuFragment: ImagesMenuFragment
    private lateinit var brushOptionsMenuFragment: BrushOptionsMenuFragment

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

        sliderLayout = view.findViewById(R.id.stroke_size_layout)

        // This will block the touch event so it will not propagate to draw view
        sliderLayout.setOnTouchListener { v, event ->
            true
        }


        menuButtons = view.findViewById(R.id.menu_buttons)

        slider = view.findViewById(R.id.stroke_size_slider)
        slider.thumbPlaceholderDrawable = ContextCompat.getDrawable(view.context, R.drawable.slider)
        slider.thumbContainerColor = Color.TRANSPARENT

        layersButton = view.findViewById(R.id.button_layers)
        layersButton.setOnClickListener {
            isLayersMenuShown = !isLayersMenuShown
            if (isLayersMenuShown) {
                showLayersMenu()
            } else {
                hideLayersMenu()
            }
        }

        layersMenuFragment = LayersMenuFragment(drawView)
        brushOptionsMenuFragment = BrushOptionsMenuFragment(drawView)
        stampOptionsMenuFragment = StampOptionsMenuFragment(strategies, drawView)
        imageMenuFragment = ImagesMenuFragment(drawView)

        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.add(R.id.layers_menu_fragment_container, layersMenuFragment)
            ?.add(R.id.menu_fragment_container, brushOptionsMenuFragment)
            ?.add(R.id.menu_fragment_container, stampOptionsMenuFragment)
            ?.add(R.id.menu_fragment_container, imageMenuFragment)
            ?.hide(layersMenuFragment)
            ?.hide(brushOptionsMenuFragment)
            ?.hide(stampOptionsMenuFragment)
            ?.hide(imageMenuFragment)
            ?.commit()

        drawView.post {
            drawView.setDimensions(drawView.width, drawView.height)
            drawView.invalidate()
            if (drawView.layerManager.getNumOfLayers() == 0) {
                drawView.layerManager.addLayer(drawView.width, drawView.height)
            }

            showBrushMenu()
        }

        view.findViewById<ImageButton>(R.id.button_ok).setOnClickListener {
            hideLayersMenu()

            drawView.saveBitmap {
                showMainLayout()
            }
        }

        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            UIUtil.showClearAllDialog(requireContext()) {
                run {
                    clearAll()
                    showMainLayout()
                }
            }
        }

        view.findViewById<Button>(R.id.button_clear_all).setOnClickListener {
            UIUtil.showClearAllDialog(requireContext()) { clearAll() }
        }

        view.findViewById<ImageButton>(R.id.button_color_picker).setOnClickListener {
            UIUtil.showColorPickerDialog(
                requireContext(),
                drawView.paintOptions.color,
            ) { envelopColor ->
                drawView.setColor(
                    envelopColor,
                )
                brushOptionsMenuFragment.changeColor(envelopColor)
                stampOptionsMenuFragment.changeColor(envelopColor)
            }
        }

        view.findViewById<ImageButton>(R.id.button_redo).setOnClickListener {
            drawView.redo()
        }
        view.findViewById<ImageButton>(R.id.button_undo).setOnClickListener {
            drawView.undo()
        }

        view.findViewById<ImageButton>(R.id.draw_view).setOnClickListener {
            it.setBackgroundResource(R.drawable.small_button)
            showBrushMenu()
        }

        view.findViewById<ImageButton>(R.id.stamp_button).setOnClickListener {
            it.setBackgroundResource(R.drawable.small_button)
            showStampMenu()
        }

        view.findViewById<ImageButton>(R.id.image_button).setOnClickListener {
            it.setBackgroundResource(R.drawable.small_button)
            showImageMenu()
        }

        slider.setOnReleaseListener {
            drawView.setStrokeWidth(it)
        }

        drawView.setStrokeWidth(slider.progress)
    }

    private fun showLayersMenu() {
        drawView.layerManager.makeLayersSemiTransparentExceptOne(drawView.layerManager.getActiveLayerIndex())
        drawView.invalidate()

        layersButton.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.selected_layers
            )
        )

        sliderLayout.visibility = View.GONE
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .show(layersMenuFragment)
            .commit()
        layersMenuFragment.updateLayersButtons(drawView.layerManager.getNumOfLayers())
    }

    private fun hideLayersMenu() {
        drawView.layerManager.resetLayersOpacity()
        drawView.layerManager.recreateLayersBitmaps()
        drawView.invalidate()

        layersButton.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.layers
            )
        )
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .hide(layersMenuFragment)
            .commit()
        sliderLayout.visibility = View.VISIBLE
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
    }

    override fun onPause() {
        super.onPause()
        drawView.stopAnimation()
    }

    private fun showBrushMenu() {
        resetMenu()

        requireActivity().supportFragmentManager.beginTransaction()
            .show(brushOptionsMenuFragment)
            .commit()
    }

    private fun showStampMenu() {
        resetMenu()

        requireActivity().supportFragmentManager.beginTransaction()
            .show(stampOptionsMenuFragment)
            .commit()
    }

    private fun showImageMenu() {
        resetMenu()

        drawView.setEditingMode()
        requireActivity().supportFragmentManager.beginTransaction()
            .show(imageMenuFragment)
            .commit()
    }

    private fun resetMenu() {
        for (i in 0 until menuButtons.childCount) {
            val child =
                menuButtons.getChildAt(i).apply {
                    background = null
                }
        }

        hideMenuFragments()
    }

    private fun hideMenuFragments() {
        requireActivity().supportFragmentManager.beginTransaction()
            .hide(brushOptionsMenuFragment)
            .hide(stampOptionsMenuFragment)
            .hide(imageMenuFragment)
            .commit()
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
            layersMenuFragment.updateLayersButtons(drawView.layerManager.getNumOfLayers())
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

package com.cvut.arfittingroom.fragment

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.UIChangeListener
import com.cvut.arfittingroom.draw.DrawHistoryHolder
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.Layer
import com.cvut.arfittingroom.draw.command.Repaintable
import com.cvut.arfittingroom.draw.model.element.impl.Curve
import com.cvut.arfittingroom.draw.model.element.impl.Gif
import com.cvut.arfittingroom.draw.model.element.impl.Image
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.model.to.drawhistory.EditorStateTO
import com.cvut.arfittingroom.model.to.drawhistory.ElementTO
import com.cvut.arfittingroom.model.to.drawhistory.LayerTO
import com.cvut.arfittingroom.service.Mapper
import com.cvut.arfittingroom.utils.FileUtil.deleteTempFiles
import com.cvut.arfittingroom.utils.UIUtil
import com.cvut.arfittingroom.utils.UIUtil.showEditorSubmenuDialog
import com.google.firebase.storage.FirebaseStorage
import com.lukelorusso.verticalseekbar.VerticalSeekBar
import io.github.muddz.styleabletoast.StyleableToast
import java.util.LinkedList
import javax.inject.Inject

class MaskEditorFragment : Fragment(), HistoryChangeListener, ColorChangeListener {
    private var backgroundBitmap: Bitmap? = null
    var editorStateTO: EditorStateTO? = null
    private var isLayersMenuShown = false
    private lateinit var drawView: DrawView
    private lateinit var sliderLayout: LinearLayout
    private lateinit var slider: VerticalSeekBar
    private lateinit var layersButton: ImageButton

    private lateinit var menuButtons: LinearLayout
    private lateinit var layersMenuFragment: LayersMenuFragment
    private lateinit var stampsMenuFragment: StampsMenuFragment
    private lateinit var imageMenuFragment: ImagesMenuFragment
    private lateinit var brushesMenuFragment: BrushesMenuFragment
    private lateinit var storage: FirebaseStorage

    private lateinit var progressBarDialog: AlertDialog
    private lateinit var progressBar: ProgressBar

    var wasDeserialized = false

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
        storage = FirebaseStorage.getInstance()

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)

        progressBarDialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

        drawView = view.findViewById(R.id.draw_view)
        drawView.applyBitmapBackground(backgroundBitmap)

        sliderLayout = view.findViewById(R.id.stroke_size_layout)
        progressBar = view.findViewById(R.id.progress_bar)

        // This will block the touch event so it will not propagate to draw view
        sliderLayout.setOnTouchListener { v, event -> true }
        view.findViewById<View>(R.id.top_ui_makeup_editor).setOnTouchListener { v, event -> true }
        view.findViewById<View>(R.id.bottom_ui_makeup_editor)
            .setOnTouchListener { v, event -> true }

        menuButtons = view.findViewById(R.id.menu_buttons)

        slider = view.findViewById(R.id.stroke_size_slider)
        slider.useThumbToSetProgress = true
        slider.thumbPlaceholderDrawable = ContextCompat.getDrawable(view.context, R.drawable.thumb)
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
        brushesMenuFragment = BrushesMenuFragment(drawView)
        stampsMenuFragment = StampsMenuFragment(strategies, drawView)
        imageMenuFragment = ImagesMenuFragment(drawView)

        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.add(R.id.layers_menu_fragment_container, layersMenuFragment)
            ?.add(R.id.menu_fragment_container, brushesMenuFragment)
            ?.add(R.id.menu_fragment_container, stampsMenuFragment)
            ?.add(R.id.menu_fragment_container, imageMenuFragment)
            ?.hide(layersMenuFragment)
            ?.hide(brushesMenuFragment)
            ?.hide(stampsMenuFragment)
            ?.hide(imageMenuFragment)
            ?.commit()

        drawView.post {
            mapper.setDimensions(drawView.width, drawView.height)
            drawView.setDimensions(drawView.width, drawView.height)
            drawView.invalidate()
            if (drawView.layerManager.getNumOfLayers() == 0) {
                drawView.layerManager.addLayer(drawView.width, drawView.height)
            }

            showBrushMenu(view.findViewById<Button>(R.id.draw_button))
        }

        view.findViewById<ImageButton>(R.id.button_ok).setOnClickListener {
            progressBar.visibility = View.VISIBLE
            hideLayersMenu()

            drawView.saveBitmap {
                showMainLayout()
                progressBar.visibility = View.INVISIBLE
            }
        }

        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            UIUtil.showClearAllDialog(requireContext()) {
                run {
                    clearAll()
                    wasDeserialized = false
                    showMainLayout()
                }
            }
        }

        view.findViewById<Button>(R.id.button_clear_all).setOnClickListener {
            UIUtil.showClearAllDialog(requireContext()) { clearAll() }
        }

        view.findViewById<ImageButton>(R.id.button_color_picker).setOnClickListener {
            UIUtil.showColorPickerDialog(
                requireActivity(),
                drawView.paintOptions.color,
                fill = drawView.paintOptions.style == Paint.Style.FILL,
                shouldShowFillCheckbox = true,
                shouldShowPipette = true,
                onPipetteSelected = { drawView.showPipetteView() }
            ) { envelopColor, fill ->
                val repaintebale = drawView.selectedElement as? Repaintable
                if (repaintebale != null) {
                    drawView.repaintElement(repaintebale, envelopColor, fill)
                }
                else {
                    drawView.setColor(
                        envelopColor,
                        fill
                    )
                    brushesMenuFragment.changeColor(envelopColor)
                    stampsMenuFragment.changeColor(envelopColor, fill)
                }
            }
        }

        DrawHistoryHolder.setHistoryChangeListener(this)
        updateUndoRedoButtons()

        view.findViewById<ImageButton>(R.id.button_redo).setOnClickListener {
            drawView.redo()
            updateUndoRedoButtons()
        }
        view.findViewById<ImageButton>(R.id.button_undo).setOnClickListener {
            drawView.undo()
            updateUndoRedoButtons()
        }

        view.findViewById<Button>(R.id.draw_button).setOnClickListener {
            showBrushMenu(it)
        }

        view.findViewById<Button>(R.id.stamp_button).setOnClickListener {
            showStampMenu(it)
        }

        view.findViewById<Button>(R.id.image_button).setOnClickListener {
            showImageMenu(it)
        }
//
//        view.findViewById<ImageButton>(R.id.button_menu).setOnClickListener {
//            showEditorSubmenuDialog(
//                requireContext(), onAddImage = { imageMenuFragment.uploadImage() },
//                onChangeBackground = { shouldShowBackground ->
//                    drawView.setFaceGridVisibility(
//                        shouldShowBackground
//                    )
//                    drawView.invalidate()
//                },
//                isBackgroundShown = drawView.getFaceGridVisibility()
//            )
//        }

        slider.setOnReleaseListener {
            drawView.setStrokeWidth(it)
        }
        drawView.setStrokeWidth(slider.progress)
        drawView.setOnColorChangeListener(this)
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
        layersMenuFragment.updateLayersButtons()
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
            drawView.post {
                if (!wasDeserialized) {
                    deserializeEditorState(it)
                    wasDeserialized = true
                }
            }
        }

        drawView.post {
            drawView.applyBitmapBackground(backgroundBitmap)
        }
        drawView.layerManager.resetAllGifs()
        drawView.layerManager.setAllGifsToStaticMode()
    }

    override fun onPause() {
        super.onPause()
        drawView.stopAnimation()
    }

    private fun showBrushMenu(button: View) {
        resetMenu()

        button.setBackgroundResource(R.drawable.small_button)
        requireActivity().supportFragmentManager.beginTransaction()
            .show(brushesMenuFragment)
            .commit()

        brushesMenuFragment.checkIfBrushSelected()
    }

    private fun showStampMenu(button: View) {
        resetMenu()

        button.setBackgroundResource(R.drawable.small_button)
        requireActivity().supportFragmentManager.beginTransaction()
            .show(stampsMenuFragment)
            .commit()

        stampsMenuFragment.checkIfStampSelected()
    }

    private fun showImageMenu(button: View) {
        resetMenu()

        button.setBackgroundResource(R.drawable.small_button)
        drawView.setEditingMode()
        requireActivity().supportFragmentManager.beginTransaction()
            .show(imageMenuFragment)
            .commit()

        imageMenuFragment.fetchImages()
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
            .hide(brushesMenuFragment)
            .hide(stampsMenuFragment)
            .hide(imageMenuFragment)
            .commit()
    }

    private fun showMainLayout() {
        val listener = context as? UIChangeListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }
        drawView.stopAnimation()
        drawView.layerManager.setAllGifsToStaticMode()
        listener.showMainLayout()
    }

    fun clearAll() {
        if (isAdded) {
            deleteTempFiles(requireContext())
            if (::drawView.isInitialized) {
                drawView.clearCanvas()
                layersMenuFragment.updateLayersButtons()
            }
        }
    }

    fun applyBackgroundBitmap(bitmap: Bitmap?) {
        backgroundBitmap = bitmap
        if (::drawView.isInitialized) {
            drawView.applyBitmapBackground(backgroundBitmap)
        }
    }

    fun serializeEditorState(): EditorStateTO {
        if (!::mapper.isInitialized) return EditorStateTO()
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
        showProgressBar()

        val errorMessages = mutableListOf<String>()

        val elementsMap = try {
            editorStateTO.elements.associateBy(
                keySelector = { it.id },
                valueTransform = { elementTO ->
                    try {
                        mapper.elementTOtoElement(elementTO)
                    } catch (e: Exception) {
                        errorMessages.add("Error deserializing element ${elementTO.id}: ${e.message}")
                        null
                    }
                }
            ).filterValues { it != null }
        } catch (e: Exception) {
            errorMessages.add("Error deserializing elements: ${e.message}")
            emptyMap()
        }

        var remainingDownloads =
            elementsMap.values.count { it is Image || it is Gif || it is Curve && it.strokeTextureRef.isNotEmpty() }

        val onDownloadComplete = {
            remainingDownloads--
            if (remainingDownloads <= 0) {
                hideProgressBar()

                if (errorMessages.isNotEmpty()) {
                    StyleableToast.makeText(
                        requireContext(),
                        errorMessages.joinToString("\n"),
                        R.style.mytoast
                    ).show()
                }

                val sortedLayers = editorStateTO.layers.sortedBy { it.index }
                val layersList: LinkedList<Layer> = LinkedList()

                val layersMap = try {
                    sortedLayers.associateBy(
                        keySelector = { it.id },
                        valueTransform = { layerTO ->
                            try {
                                val layer = mapper.layerTOtoLayer(layerTO)
                                layersList.add(layer)
                                layerTO.elements.forEach {
                                    elementsMap[it]?.let { element -> layer.addElement(element) }
                                }
                                layer
                            } catch (e: Exception) {
                                errorMessages.add("Error deserializing layer ${layerTO.id}: ${e.message}")
                                null
                            }
                        }
                    ).filterValues { it != null }
                } catch (e: Exception) {
                    errorMessages.add("Error deserializing layers: ${e.message}")
                    emptyMap()
                }

                drawView.layerManager.deleteLayers()
                drawView.layerManager.layers.addAll(layersList)

                if (errorMessages.isNotEmpty()) {
                    StyleableToast.makeText(
                        requireContext(),
                        "Drawing was opened with errors",
                        R.style.mytoast
                    ).show()
                }
            }
        }

        elementsMap.values.forEach {
            if (remainingDownloads == 0) {
                onDownloadComplete()
            }
            if (it is Image) {
                imageMenuFragment.downloadImage(it.resourceRef, onDownloadComplete) { bitmap, _ ->
                    it.bitmap = bitmap
                }
            } else if (it is Gif) {
                imageMenuFragment.downloadGif(
                    it.resourceRef,
                    onDownloadComplete
                ) { gifDrawable, _ ->
                    it.setDrawable(gifDrawable)
                }
            } else if (it is Curve && it.strokeTextureRef.isNotEmpty()) {
                imageMenuFragment.downloadImage(
                    it.strokeTextureRef,
                    onDownloadComplete
                ) { bitmap, _ ->
                    it.setTextureBitmap(bitmap)
                }
            }
        }
    }


    private fun showProgressBar() {
        progressBarDialog.show()
    }

    private fun hideProgressBar() {
        progressBarDialog.dismiss()
        drawView.invalidate()
    }

    private fun updateUndoRedoButtons() {
        val isRedoActive = DrawHistoryHolder.getUndoneActionsSize() != 0
        val isUndoActive = DrawHistoryHolder.getHistorySize() != 0

        val redo = requireView().findViewById<ImageButton>(R.id.button_redo)

        if (isRedoActive) {
            redo.imageTintList = ColorStateList.valueOf(Color.WHITE)
        } else {
            redo.imageTintList = ColorStateList.valueOf(Color.GRAY)
        }

        val undo = requireView().findViewById<ImageButton>(R.id.button_undo)

        if (isUndoActive) {
            undo.imageTintList = ColorStateList.valueOf(Color.WHITE)
        } else {
            undo.imageTintList = ColorStateList.valueOf(Color.GRAY)
        }
    }

    companion object {
        const val MAKEUP_EDITOR_FRAGMENT_TAG = "MakeupEditorFragmentTag"
    }

    override fun onHistoryChanged() {
        updateUndoRedoButtons()
    }

    override fun onColorChanged(newColor: Int, fill: Boolean) {
        brushesMenuFragment.changeColor(newColor)
        stampsMenuFragment.changeColor(newColor, fill)
    }
}

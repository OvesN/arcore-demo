package com.cvut.arfittingroom.draw.service

import android.graphics.Bitmap
import android.graphics.Canvas
import com.cvut.arfittingroom.draw.Layer
import com.cvut.arfittingroom.draw.command.action.element.impl.MoveElementBetweenLayers
import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.path.DrawablePath
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Layer manager
 *
 */
class LayerManager {
    val layers = mutableListOf<Layer>()
    private val idToLayerMap = HashMap<UUID, Layer>()
    private var activeLayerIndex = 0
    private var layersBelowActiveLayerBitmap: Bitmap? = null
    private var layersAboveActiveLayerBitmap: Bitmap? = null
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    var bitmapFromAllLayers: Bitmap? = null

    /**
     * Draw all layers
     * The drawing proceeds in the following steps:
     * 1. Draw all layers below the active layer
     * 2. Draw active layer
     * 3. Draw all layers above the active layer
     *
     * @param canvas on which to draw the layers
     * @param paintOptions for current finger painting that the user is creating
     */
    fun drawLayers(
        canvas: Canvas,
        paintOptions: PaintOptions,
    ) {
        if (layers.isEmpty()) {
            return
        }

        layersBelowActiveLayerBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        layers[activeLayerIndex].draw(canvas, paintOptions)

        layersAboveActiveLayerBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    fun setDimensions(
        width: Int,
        height: Int,
    ) {
        viewWidth = width
        viewHeight = height
    }

    fun updateLayersBitmaps() {
        layers.forEach {
            it.prepareBitmap()
        }
    }

    fun getActiveLayerId() = layers[activeLayerIndex].id

    fun getActiveLayerIndex() = activeLayerIndex

    fun isVisible(layerIndex: Int) = layers[layerIndex].isVisible

    fun makeLayersSemiTransparentExceptOne(layerIndex: Int) {
        makeLayersSemiTransparent()
        setLayerOpacity(1f, layerIndex)
        recreateLayersBitmaps()
    }

    // Returns index of the last layer
    fun addLayer(
        width: Int,
        height: Int,
        layerId: UUID? = null,
    ): Int {
        val layer =
            layerId?.let { Layer(layerId, width, height) } ?: Layer(width = width, height = height)

        if (layers.isNotEmpty()) {
            layers[activeLayerIndex].deselectAllElements()
        }

        idToLayerMap[layer.id] = layer

        layers.add(layer)

        activeLayerIndex = layers.size - 1
        return activeLayerIndex
    }

    fun addElementToLayer(
        index: Int = 0,
        element: Element,
        layer: Layer? = null,
        layerId: UUID? = null,
    ) {
        val foundLayer =
            layer
                ?: layerId.let { idToLayerMap[layerId] }
                ?: layers.getOrNull(index)

        foundLayer?.addElement(element)
    }

    fun getLayerIdByIndex(index: Int): UUID? = layers.getOrNull(index)?.id

    fun toggleActiveLayerVisibility() {
        if (layers.isEmpty()) {
            return
        }
        layers[activeLayerIndex].isVisible = !layers[activeLayerIndex].isVisible
    }

    fun removeElementFromLayer(
        elementId: UUID,
        index: Int = 0,
        layer: Layer? = null,
        layerId: UUID? = null,
    ) {
        val foundLayer =
            layer
                ?: layerId.let { idToLayerMap[layerId] }
                ?: layers.getOrNull(index)
                ?: return

        foundLayer.removeElement(elementId)
    }

    fun removeLayer(
        index: Int = 0,
        layerId: UUID? = null,
    ) {
        val layerToRemove =
            layerId?.let { idToLayerMap[layerId] }
                ?: layers.getOrNull(index)
                ?: return

        idToLayerMap.remove(layerToRemove.id)
        layers.removeAt(index)

        logger.info { "Layer from index $index removed" }
    }

    fun setLayerVisibility(
        index: Int,
        isVisible: Boolean,
    ) {
        layers[index].isVisible = isVisible
    }

    // Returns true if layer was moved
    fun canMoveLayer(
        fromIndex: Int,
        toIndex: Int,
    ): Boolean {
        // Check for no movement or invalid indices
        if (fromIndex == toIndex ||
            fromIndex < 0 || toIndex < 0 ||
            toIndex > layers.size ||
            fromIndex == layers.size - 1 && toIndex == layers.size
        ) {
            return false
        }

        val layer = layers.removeAt(fromIndex)
        layers.add(toIndex, layer)

        return true
    }

    fun getNumOfLayers() = layers.size

    fun getCurPath() = layers[activeLayerIndex].curPath

    fun setCurPath(path: DrawablePath) {
        layers[activeLayerIndex].curPath = path
    }

    fun deleteLayers() {
        layers.clear()
        idToLayerMap.clear()
    }

    fun selectElement(
        x: Float,
        y: Float,
    ): Element? {
        deselectAllElements()

        val element = layers[activeLayerIndex].findFirstIntersectedElement(x, y)
        element?.setSelected(true)

        element?.let {
            setUpdatableElement(it)
        }

        return element
    }

    fun deselectAllElements() {
        if (activeLayerIndex >= layers.size) {
            return
        }
        layers[activeLayerIndex].deselectAllElements()
    }

    fun moveElementUp(element: Element): MoveElementBetweenLayers? {
        // If element is on already top layer do nothing
        return if (activeLayerIndex == layers.size - 1) {
            null
        } else {
            MoveElementBetweenLayers(
                element.id,
                element,
                oldLayerId = getActiveLayerId(),
                newLayerId = layers[activeLayerIndex + 1].id,
                this,
            )
        }
    }

    fun moveElementDown(element: Element): MoveElementBetweenLayers? {
        // If element is on already bottom layer do nothing
        return if (activeLayerIndex == 0) {
            null
        } else {
            MoveElementBetweenLayers(
                element.id,
                element,
                oldLayerId = getActiveLayerId(),
                newLayerId = layers[activeLayerIndex - 1].id,
                this,
            )
        }
    }

    fun moveElementTo(
        element: Element,
        layerIndex: Int,
    ): MoveElementBetweenLayers? =
        if (layerIndex == activeLayerIndex) {
            null
        } else {
            MoveElementBetweenLayers(
                element.id,
                element,
                oldLayerId = getActiveLayerId(),
                newLayerId = layers[layerIndex].id,
                this,
            )
        }

    fun resetLayersOpacity() {
        layers.forEach { it.setOpacity(1f) }
    }

    fun makeLayersSemiTransparent() {
        layers.forEach { layer ->

            layer.setOpacity(0.5f)
        }
    }

    fun setLayerOpacity(opacity: Float, layerIndex: Int) {
        if ( layerIndex >= layers.size|| layerIndex < 0) {
            return
        }
        layers[layerIndex].setOpacity(opacity)
    }


    /**
     * Initiates the process of separating the elements that
     * should be drawn below and above the [element] into two distinct bitmaps on the active layer
     *
     * @param element that will be redrawn on every OnDraw
     */
    fun setUpdatableElement(element: Element) {
        val activeLayer =
            layers[activeLayerIndex].apply {
                elementToUpdate = element
            }
        activeLayer.prepareBitmaps()
    }

    /**
     * Set active layer
     * The active layer is the one that the user is currently drawing on.
     *
     * @param index of the new active layer
     */
    fun setActiveLayer(index: Int) {
        if (index < 0 || index >= layers.size) {
            return
        } else {
            deselectAllElements()

            val activeLayer = layers[index]

            activeLayer.resetBitmaps()
            resetBitmaps()

            val layersBelow = layers.subList(0, index)
            val layersAbove =
                if (index + 1 <= layers.lastIndex) {
                    layers.subList(
                        index + 1,
                        layers.size,
                    )
                } else {
                    listOf()
                }

            layersBelowActiveLayerBitmap = createBitmapFromLayers(layersBelow)
            layersAboveActiveLayerBitmap = createBitmapFromLayers(layersAbove)

            activeLayer.prepareBitmap()
            activeLayerIndex = index
        }
    }

    /**
     * Creates a bitmap from the given [layers]
     *
     * @param layers The layers to be merged
     * @return A bitmap that merges all the provided layers into one
     * or null if there is nothing to draw
     */
    private fun createBitmapFromLayers(layers: List<Layer>): Bitmap? {
        if (layers.isEmpty()) {
            return null
        }

        val bitmap =
            Bitmap.createBitmap(
                viewWidth,
                viewHeight,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)

        layers.forEach { layer ->
            val layerBitmap = layer.createBitmap()
            layerBitmap?.let { canvas.drawBitmap(layerBitmap, 0f, 0f, layer.opacityPaint) }
        }

        return bitmap
    }

    private fun resetBitmaps() {
        layersBelowActiveLayerBitmap?.recycle()
        layersAboveActiveLayerBitmap?.recycle()
        layersBelowActiveLayerBitmap = null
        layersAboveActiveLayerBitmap = null
    }

    fun recreateLayersBitmaps() {
        setActiveLayer(activeLayerIndex)
    }

    fun restoreDeletedLayer(layerId: UUID) {
        // TODO
    }

    fun doesContainAnyGif() = layers.any { it.doesHaveGif() }

    fun getMaxNumberOfFrames() =
        layers.maxOfOrNull {
            it.getMaxNumberOfFrames()
        } ?: 0

    fun createBitmapFromAllLayers() =
        createBitmapFromLayers(layers) ?: Bitmap.createBitmap(
            viewWidth,
            viewHeight,
            Bitmap.Config.ARGB_8888,
        )

    fun setAllGifsToAnimationMode() {
        layers.forEach { it.setAllGifsToAnimationMode() }
    }

    fun setAllGifsToStaticMode() {
        layers.forEach { it.setAllGifsToStaticMode() }
    }

    fun resetAllGifs() {
        layers.forEach { it.resetAllGifs() }
    }

}

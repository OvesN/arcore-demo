package cz.cvut.arfittingroom.draw.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.Layer
import cz.cvut.arfittingroom.draw.command.action.element.impl.MoveElementBetweenLayers
import cz.cvut.arfittingroom.draw.model.PaintOptions
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import mu.KotlinLogging
import java.util.UUID


private val logger = KotlinLogging.logger {}

class LayerManager {
    private val layers = mutableListOf<Layer>()
    private val idToLayerMap = HashMap<UUID, Layer>()

    private var activeLayerIndex = 0

    private var layersBelowActiveLayerBitmap: Bitmap? = null
    private var layersAboveActiveLayerBitmap: Bitmap? = null

    //TODO resolve but how?
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    fun setScreenMetrics(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

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
    fun drawLayers(canvas: Canvas, paintOptions: PaintOptions) {
        if (layers.isEmpty()) return

        layersBelowActiveLayerBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        layers[activeLayerIndex].draw(canvas, paintOptions)

        layersAboveActiveLayerBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    fun getActiveLayerId() =
        layers[activeLayerIndex].id

    fun getActiveLayerIndex() =
        activeLayerIndex

    // Returns index of the last layer
    fun addLayer(width: Int, height: Int, layerId: UUID? = null): Int {
        val layer = layerId?.let { Layer(width, height, layerId) } ?: Layer(width, height)

        if (layers.isNotEmpty()) {
            layers[activeLayerIndex].deselectAllElements()
        }

        idToLayerMap[layer.id] = layer

        layers.add(layer)

        activeLayerIndex = layers.size - 1
        return activeLayerIndex
    }

    //Returns layer id to which element was added
    fun addElementToLayer(
        index: Int = 0,
        element: Element,
        layer: Layer? = null,
        layerId: UUID? = null
    ): UUID? {
        val foundLayer = layer
            ?: layerId.let { idToLayerMap[layerId] }
            ?: layers.getOrNull(index)
            ?: return null

        foundLayer.addElement(element)

        return foundLayer.id
    }

    fun removeElementFromLayer(
        elementId: UUID,
        index: Int = 0,
        layer: Layer? = null,
        layerId: UUID? = null
    ) {
        val foundLayer = layer
            ?: layerId.let { idToLayerMap[layerId] }
            ?: layers.getOrNull(index)
            ?: return

        foundLayer.removeElement(elementId)
    }

    fun removeLayer(index: Int = 0, layerId: UUID? = null) {
        val layerToRemove = layerId?.let { idToLayerMap[layerId] }
            ?: layers.getOrNull(index)
            ?: return

        idToLayerMap.remove(layerToRemove.id)
        layers.removeAt(index)

        logger.info { "Layer from index $index removed" }
    }

    fun setLayerVisibility(index: Int, isVisible: Boolean) {
        layers[index].isVisible = isVisible
    }

    // Returns true if layer was moved
    fun moveLayer(fromIndex: Int, toIndex: Int): Boolean {
        // Check for no movement or invalid indices
        if (fromIndex == toIndex
            || fromIndex < 0 || toIndex < 0
            || toIndex > layers.size
            || fromIndex == layers.size - 1 && toIndex == layers.size
        ) {
            logger.info { "Layer does not need to be moved" }
            return false
        }

        val layer = layers.removeAt(fromIndex)
        layers.add(toIndex, layer)

        logger.info { "Layer moved from index $fromIndex to index $toIndex" }
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

    fun selectElement(x: Float, y: Float): Element? {
        deselectAllElements()

        val element = layers[activeLayerIndex].findFirstIntersectedElement(x, y)
        element?.isSelected = true

        return element
    }

    fun deselectAllElements() {
        layers[activeLayerIndex].deselectAllElements()
    }

    fun moveElementUp(element: Element): MoveElementBetweenLayers? {
        //If element is on already top layer do nothing
        return if (activeLayerIndex == layers.size - 1) {
            null
        } else {
            MoveElementBetweenLayers(
                element.id,
                element,
                oldLayerId = getActiveLayerId(),
                newLayerId = layers[activeLayerIndex + 1].id,
                this
            )
        }
    }

    fun moveElementDown(element: Element): MoveElementBetweenLayers? {
        //If element is on already bottom layer do nothing
        return if (activeLayerIndex == 0) {
            null
        } else {
            MoveElementBetweenLayers(
                element.id,
                element,
                oldLayerId = getActiveLayerId(),
                newLayerId = layers[activeLayerIndex - 1].id,
                this
            )
        }
    }

    fun moveElementTo(element: Element, layerIndex: Int): MoveElementBetweenLayers? {
        return if (layerIndex == activeLayerIndex) {
            null
        } else {
            MoveElementBetweenLayers(
                element.id,
                element,
                oldLayerId = getActiveLayerId(),
                newLayerId = layers[layerIndex].id,
                this
            )
        }
    }

    /**
     * Initiates the process of separating the elements that
     * should be drawn below and above the [element] into two distinct bitmaps on the active layer
     */
    fun startElementContinuousChanging(element: Element) {
        val activeLayer = layers[activeLayerIndex]
        activeLayer.prepareBitmaps(element)
    }

    /**
     * Set active layer
     * The active layer is the one that the user is currently drawing on.
     *
     * @param index of the new active layer
     */
    fun setActiveLayer(index: Int) {
        if (index > layers.size - 1) {
            return
        } else {
            val activeLayer = layers[index]

            activeLayer.resetBitmaps()
            resetBitmaps()

            val layersBelow = layers.subList(0, index)
            val layersAbove = layers.subList(index + 1, layers.lastIndex)

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
     */
    private fun createBitmapFromLayers(layers: List<Layer>): Bitmap {
        val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        layers.forEach {
            canvas.drawBitmap(it.createBitmap(), 0f, 0f, it.opacityPaint)
        }

        return bitmap
    }

    private fun resetBitmaps() {
        layersBelowActiveLayerBitmap?.recycle()
        layersAboveActiveLayerBitmap?.recycle()
        layersBelowActiveLayerBitmap = null
        layersAboveActiveLayerBitmap = null
    }

    fun restoreDeletedLayer(layerId: UUID) {

    }

}
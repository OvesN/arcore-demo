package cz.cvut.arfittingroom.draw.service

import android.graphics.Bitmap
import android.graphics.Canvas
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

    fun drawLayers(canvas: Canvas, paintOptions: PaintOptions) {
        if (layers.isEmpty()) return

        layersBelowActiveLayerBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // Change paint for finger drawing
        layers[activeLayerIndex].changePaint(paintOptions)
        layers[activeLayerIndex].draw(canvas)

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
     * should be drawn before and after [element] into two distinct bitmaps on the active layer
     */
    fun startElementContinuousChanging(element: Element) {
        val activeLayer = layers[activeLayerIndex]

        activeLayer
    }

    /**
     * When the selected element stopped to change continuously,
     * all elements on the active layer should be merged into one bitmap
     */
    fun endElementContinuousChanging() {

    }

    /**
     * Set active layer
     * When the new active layer is selected,
     *
     * @param index of the new active layer
     */
    fun setActiveLayer(index: Int) {
        if (index > layers.size - 1) {
            return
        } else {
            val activeLayer = layers[index]
            activeLayer.resetBitmaps()

            activeLayerIndex = index

        }
    }

    fun restoreDeletedLayer(layerId: UUID) {

    }

}
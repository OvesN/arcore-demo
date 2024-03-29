package cz.cvut.arfittingroom.draw.service

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.Layer
import cz.cvut.arfittingroom.draw.model.PaintOptions
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

class LayerManager {
    private val layers = mutableListOf<Layer>()
    private val idToLayerMap = HashMap<UUID, Layer>()

    var activeLayerIndex = 0

    fun getActiveLayerId() =
        layers[activeLayerIndex].id

    // Returns index of the last layer
    fun addLayer(width: Int, height: Int): Int {
        val layer = Layer(width, height)

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
        if (index >= layers.size) {
            return null
        }

        val foundLayer = layer ?: layerId.let { idToLayerMap[layerId] } ?: layers[index]

        foundLayer.addElement(element)

        return foundLayer.id
    }

    fun removeElementFromLayer(
        elementId: UUID,
        index: Int = 0,
        layer: Layer? = null,
        layerId: UUID? = null
    ) {
        if (index >= layers.size) {
            return
        }

        val foundLayer = layer ?: layerId.let { idToLayerMap[layerId] } ?: layers[index]
        foundLayer.removeElement(elementId)
    }

    fun removeLayer(index: Int) {
        //TODO remove layer action?
        if (index >= layers.size) {
            return
        }
        val layerToRemove = layers[index]
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

    fun drawLayers(canvas: Canvas, paintOptions: PaintOptions) {
        layers[activeLayerIndex].changePaint(paintOptions)
        layers.forEach { layer ->
            layer.draw(canvas)
        }
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
}
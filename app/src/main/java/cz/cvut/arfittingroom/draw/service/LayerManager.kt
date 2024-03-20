package cz.cvut.arfittingroom.draw.service

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.gelLastAction
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.getLastUndoneAction
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.globalDrawHistory
import cz.cvut.arfittingroom.draw.Layer
import cz.cvut.arfittingroom.draw.PaintOptions
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import mu.KotlinLogging
import java.util.LinkedList
import java.util.UUID

private val logger = KotlinLogging.logger {}

class LayerManager {
    private val layers = mutableListOf<Layer>()
    private val idToLayerMap = HashMap<UUID, Layer>()

    var activeLayerIndex = 0

    fun undo() {
        val action = gelLastAction()
        if (action != null) {
            val relatedLayer = idToLayerMap[action.layerId]
            require(relatedLayer != null)

            relatedLayer.actions[action.element.id]?.remove(action)
        }
    }

    fun redo() {
        val action = getLastUndoneAction()
        if (action != null) {
            val relatedLayer = idToLayerMap[action.layerId]
            require(relatedLayer != null)

            addToLayer(command = action, layer = relatedLayer)
        }
    }

    // Returns index of the last layer
    fun addLayer(width: Int, height: Int): Int {
        val layer = Layer(width, height)
        idToLayerMap[layer.id] = layer

        layers.add(layer)

        activeLayerIndex = layers.size - 1
        return activeLayerIndex
    }

    fun moveToLayer(element: Element, index: Int) {
        if (index >= layers.size) {
            return
        }

        val relatedActions = globalDrawHistory.filter { it.element.id == element.id }.toList()
        layers[index].addElement(element, relatedActions)
    }

    fun addToLayer(index: Int = 0, command: Command<out Element>, layer: Layer? = null) {
        if (index >= layers.size) {
            return
        }

        globalDrawHistory.add(command)

        val foundLayer = layer ?: layers[index]

        command.layerId = foundLayer.id
        foundLayer.elements.putIfAbsent(command.element.id, command.element)
        val commandList = foundLayer.actions.getOrPut(command.element.id) { LinkedList() }

        commandList.add(command)
    }

    fun removeFromLayer(elementId: UUID, index: Int = 0, layer: Layer? = null) {
        if (index >= layers.size) {
            return
        }

        val foundLayer = layer ?: layers[index]
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
}
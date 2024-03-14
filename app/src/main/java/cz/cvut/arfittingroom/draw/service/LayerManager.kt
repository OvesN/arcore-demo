package cz.cvut.arfittingroom.draw.service

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.gelLastAction
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.getLastUndoneAction
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.globalDrawHistory
import cz.cvut.arfittingroom.draw.Layer
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.LinkedList
import java.util.UUID

class LayerManager {
    private val layers = mutableListOf<Layer>()
    private val idToLayerMap = HashMap<UUID, Layer>()

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

    fun addLayer(width: Int, height: Int): Layer {
        val layer = Layer(width, height)
        idToLayerMap[layer.id] = layer

        layers.add(layer)
        return layer
    }

    fun moveToLayer(element: Element, index: Int) {
        val relatedActions = globalDrawHistory.filter { it.element.id == element.id }.toList()
        layers[index].addElement(element, relatedActions)
    }

    fun addToLayer(index: Int = 0, command: Command<out Element>, layer: Layer? = null) {
        globalDrawHistory.add(command)

        val foundLayer = layer ?: layers[index]

        command.layerId = foundLayer.id
        foundLayer.elements.putIfAbsent(command.element.id, command.element)
        val commandList = foundLayer.actions.getOrPut(command.element.id) { LinkedList() }

        commandList.add(command)
    }

    fun removeFromLayer(elementId: UUID, index: Int = 0, layer: Layer? = null) {
        val foundLayer = layer ?: layers[index]
        foundLayer.removeElement(elementId)
    }

    fun removeLayer(index: Int) {
        val layerToRemove = layers[index]
        idToLayerMap.remove(layerToRemove.id)
        layers.removeAt(index)
    }

    fun setLayerVisibility(index: Int, isVisible: Boolean) {
        layers[index].isVisible = isVisible
    }

    fun moveLayer(fromIndex: Int, toIndex: Int) {
        val layer = layers.removeAt(fromIndex)
        layers.add(toIndex, layer)
    }

    fun drawLayers(canvas: Canvas) {
        layers.forEach { layer ->
            layer.draw(canvas)
        }
    }
}
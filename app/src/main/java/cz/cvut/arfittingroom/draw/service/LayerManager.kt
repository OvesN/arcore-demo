package cz.cvut.arfittingroom.draw.service

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.actions
import cz.cvut.arfittingroom.draw.Layer
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class LayerManager {
    private val layers = mutableListOf<Layer>()

    fun getLayerById(id: UUID): Layer? = layers.find { it.id == id }

    fun addLayer(width: Int, height: Int): Layer {
        val layer = Layer(width, height)
        layers.add(layer)
        return layer
    }

    fun addToLayer(element: Element, index: Int) {
        val relatedActions = actions.filter { it.element.id == element.id }.toList()
        layers[index].addElement(element, relatedActions)
    }

    fun removeFromLayer(elementId: UUID, index: Int) {
        layers[index].removeElement(elementId)
    }

    fun removeLayer(index: Int) {
        if (index in layers.indices) {
            layers.removeAt(index)
        }
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
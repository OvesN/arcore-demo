package cz.cvut.arfittingroom.service

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.Layer

class LayerManager {
    private val layers = mutableListOf<Layer>()

    fun addLayer(width: Int, height: Int): Layer {
        val layer = Layer(width, height)
        layers.add(layer)
        return layer
    }

    fun removeLayer(index: Int) {
        if (index in layers.indices) {
            layers.removeAt(index)
        }
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
package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.service.LayerManager

class AddLayer(
    private val width: Int,
    private val height: Int,
    private val layerManager: LayerManager,
) : Command {
    override val description: String = "add new layer"
    private var layerId: Int = 0

    override fun execute() {
        layerId = layerManager.addLayer(width, height)
    }

    override fun revert() {
        layerManager.removeLayer(layerId)
    }
}

package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class AddLayer(
    private val width: Int,
    private val height: Int,
    private val layerManager: LayerManager,
    private val layerId: UUID,
) : Command {
    override val description: String = "add new layer"

    override fun execute() {
        layerManager.addLayer(width, height, layerId)
    }

    override fun revert() {
        layerManager.removeLayer(layerId = layerId)
    }
}

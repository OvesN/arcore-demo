package cz.cvut.arfittingroom.draw.command.action.layer.impl

import cz.cvut.arfittingroom.draw.command.action.layer.LayerCommand
import cz.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class AddLayer(
    private val width: Int,
    private val height: Int,
    override val layerManager: LayerManager,
    override val layerId: UUID
) :
    LayerCommand() {
    override fun execute() {
        layerManager.addLayer(width, height, layerId)
    }

    override fun revert() {
        layerManager.removeLayer(layerId = layerId)
    }
}
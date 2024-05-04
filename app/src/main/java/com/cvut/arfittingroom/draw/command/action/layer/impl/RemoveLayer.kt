package com.cvut.arfittingroom.draw.command.action.layer.impl

import com.cvut.arfittingroom.draw.command.action.layer.LayerCommand
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class RemoveLayer(override val layerManager: LayerManager, override val layerId: UUID) :
    LayerCommand() {
    override val description: String = "remove layer"

    override fun execute() {
        layerManager.removeLayer(layerId = layerId)
    }

    override fun revert() {
        layerManager.restoreDeletedLayer(layerId = layerId)
    }
}

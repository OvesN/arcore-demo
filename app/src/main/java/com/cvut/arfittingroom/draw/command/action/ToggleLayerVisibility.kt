package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class ToggleLayerVisibility(
    layerIndex: Int,
    private val layerId: UUID,
    private val layerManager: LayerManager
) : Command {
    override val description: String = "layer $layerIndex is hidden"

    override fun execute() {
        layerManager.toggleLayerVisibility(layerId)
    }

    override fun revert() {
        layerManager.toggleLayerVisibility(layerId)
    }
}
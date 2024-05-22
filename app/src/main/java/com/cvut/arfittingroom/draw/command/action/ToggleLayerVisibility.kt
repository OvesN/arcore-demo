package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.service.LayerManager

/**
 * Command to hide/reveal the specific layer
 *
 * @property layerId
 * @property layerManager
 * @constructor
 *
 * @param layerIndex
 *
 * @author Veronika Ovsyannikova
 */
class ToggleLayerVisibility(
    layerIndex: Int,
    private val layerId: Int,
    private val layerManager: LayerManager,
) : Command {
    override val description: String = "layer $layerIndex is hidden"

    override fun execute() {
        layerManager.toggleLayerVisibility(layerId)
    }

    override fun revert() {
        layerManager.toggleLayerVisibility(layerId)
    }
}

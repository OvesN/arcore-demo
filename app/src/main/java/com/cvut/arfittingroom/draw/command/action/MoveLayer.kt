package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.service.LayerManager

/**
 * Move layer
 *
 * @property layerManager
 * @property fromIndex
 * @property toIndex
 *
 * @author Veronika Ovsyannikova
 */
class MoveLayer(
    private val layerManager: LayerManager,
    private val fromIndex: Int,
    private val toIndex: Int,
) : Command {
    override val description: String = "move layer from $fromIndex to $toIndex"

    override fun execute() {
        layerManager.moveLayer(fromIndex, toIndex)
    }

    override fun revert() {
        layerManager.moveLayer(toIndex, fromIndex)
    }
}

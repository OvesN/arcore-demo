package com.cvut.arfittingroom.draw.command.action.layer.impl

import com.cvut.arfittingroom.draw.command.action.layer.LayerCommand
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class MoveLayer(
    override val layerManager: LayerManager,
    private val fromIndex: Int,
    private val toIndex: Int,
    override val layerId: UUID,
) :
    LayerCommand() {
    override val description: String = "move layer from $fromIndex to $toIndex"

    override fun execute() {
        layerManager.canMoveLayer(fromIndex, toIndex)
    }

    override fun revert() {
        layerManager.canMoveLayer(toIndex, fromIndex)
    }
}

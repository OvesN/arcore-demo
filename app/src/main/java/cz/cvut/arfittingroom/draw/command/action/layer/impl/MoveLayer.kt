package cz.cvut.arfittingroom.draw.command.action.layer.impl

import cz.cvut.arfittingroom.draw.command.action.layer.LayerCommand
import cz.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class MoveLayer(
    override val layerManager: LayerManager,
    private val fromIndex: Int,
    private val toIndex: Int,
    override val layerId: UUID) :
    LayerCommand() {
    override fun execute() {
        layerManager.moveLayer(fromIndex, toIndex)
    }

    override fun revert() {
        layerManager.moveLayer(toIndex, fromIndex)
    }
}
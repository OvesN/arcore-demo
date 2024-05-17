package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class MoveElementBetweenLayers(
    private val elementId: UUID,
    private val element: Element,
    private val oldLayerId: UUID,
    private val newLayerId: UUID,
    newLayerIndex: Int,
    oldLayerIndex: Int,
    private val layerManager: LayerManager,
) : Command {
    override val description: String =
        "move ${element.name} from layer $oldLayerIndex to $newLayerIndex"

    override fun execute() {
        layerManager.removeElementFromLayer(
            layerId = oldLayerId,
            elementId = element.id,
        )
        layerManager.addElementToLayer(
            layerId = newLayerId,
            element = element,
        )
        layerManager.recreateLayersBitmaps()
    }

    override fun revert() {
        layerManager.removeElementFromLayer(
            layerId = newLayerId,
            elementId = element.id,
        )
        layerManager.addElementToLayer(
            layerId = oldLayerId,
            element = element,
        )
        layerManager.recreateLayersBitmaps()
    }
}

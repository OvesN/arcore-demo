package com.cvut.arfittingroom.draw.command.action.element.impl

import com.cvut.arfittingroom.draw.command.action.element.ElementCommand
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class MoveElementBetweenLayers(
    override val elementId: UUID,
    private val element: Element,
    private val oldLayerId: UUID,
    private val newLayerId: UUID,
    private val layerManager: LayerManager,
) : ElementCommand() {
    override val description: String = "move ${element.name} from layer $oldLayerId to $newLayerId"

    override fun execute() {
        layerManager.removeElementFromLayer(
            layerId = oldLayerId,
            elementId = element.id,
        )
        layerManager.addElementToLayer(
            layerId = newLayerId,
            element = element,
        )
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
    }
}

package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class RemoveElementFromLayer(
    private val elementId: UUID,
    private val element: Element,
    private val layerManager: LayerManager,
    private val layerId: UUID,
) : Command {
    override val description: String = "remove ${element.name}"

    override fun execute() {
        layerManager.removeElementFromLayer(element.id, layerId = layerId)
    }

    override fun revert() {
        layerManager.addElementToLayer(element = element, layerId = layerId)
    }
}

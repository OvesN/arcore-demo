package com.cvut.arfittingroom.draw.command.action.element.impl

import com.cvut.arfittingroom.draw.command.action.element.ElementCommand
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class RemoveElementFromLayer(
    override val elementId: UUID,
    private val element: Element,
    private val layerManager: LayerManager,
    private val layerId: UUID,
) : ElementCommand() {
    override val description: String = "remove ${element.name}"

    override fun execute() {
        layerManager.removeElementFromLayer(element.id, layerId = layerId)
    }

    override fun revert() {
        layerManager.addElementToLayer(element = element, layerId = layerId)
    }
}

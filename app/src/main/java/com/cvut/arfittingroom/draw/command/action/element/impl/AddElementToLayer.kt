package com.cvut.arfittingroom.draw.command.action.element.impl

import com.cvut.arfittingroom.draw.command.action.element.ElementCommand
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class AddElementToLayer(
    override val elementId: UUID,
    private val element: Element,
    private val layerManager: LayerManager,
    private val layerId: UUID,
) : ElementCommand() {
    override val description: String = "add ${element.name}"

    override fun execute() {
        layerManager.addElementToLayer(element = element, layerId = layerId)
    }

    override fun revert() {
        layerManager.removeElementFromLayer(elementId = element.id, layerId = layerId)
    }
}

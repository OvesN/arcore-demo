package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager

class AddElementToLayer(
    private val element: Element,
    private val layerManager: LayerManager,
    private val layerId: Int,
) : Command {
    override val description: String = "add ${element.name}"

    override fun execute() {
        layerManager.addElementToLayer(element = element, layerId = layerId)
    }

    override fun revert() {
        layerManager.removeElementFromLayer(elementId = element.id, layerId = layerId)
    }
}

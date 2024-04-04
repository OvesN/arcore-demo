package cz.cvut.arfittingroom.draw.command.action.element.impl

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.action.element.ElementCommand
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class RemoveElementFromLayer(
    override val elementId: UUID,
    private val element: Element,
    private val layerManager: LayerManager,
    private val layerId: UUID
) : ElementCommand() {
    override fun execute() {
        layerManager.removeElementFromLayer(element.id, layerId = layerId)
    }

    override fun revert() {
        layerManager.addElementToLayer(element = element, layerId = layerId)
    }
}
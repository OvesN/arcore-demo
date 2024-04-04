package cz.cvut.arfittingroom.draw.command.action

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class MoveElementBetweenLayers<T>(
    override val element: T,
    private val oldLayerId: UUID,
    private val newLayerId: UUID,
    private val layerManager: LayerManager
) :
    Command<T> where T : Element {
    override fun execute() {
        layerManager.removeElementFromLayer(
            layerId = oldLayerId,
            elementId = element.id,)
        layerManager.addElementToLayer(
            layerId = newLayerId,
            element = element,
        )
    }

    override fun revert() {
        layerManager.removeElementFromLayer(
            layerId = newLayerId,
            elementId = element.id
        )
        layerManager.addElementToLayer(
            layerId = oldLayerId,
            element = element
        )
    }

}
package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager

/**
 * Move element between layers
 *
 * @property element
 * @property oldLayerId
 * @property newLayerId
 * @property layerManager
 *
 * @author Veronika Ovsyannikova
 */
class MoveElementBetweenLayers(
    private val element: Element,
    private val oldLayerId: Int,
    private val newLayerId: Int,
    private val layerManager: LayerManager,
) : Command {
    override val description: String =
        "move ${element.name} from layer $oldLayerId to $newLayerId"

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

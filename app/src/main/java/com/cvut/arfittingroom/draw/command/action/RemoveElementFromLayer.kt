package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.service.LayerManager

/**
 * Remove element from layer
 *
 * @property element
 * @property layerManager
 * @property layerId
 *
 * @author Veronika Ovsyannikova
 */
class RemoveElementFromLayer(
    private val element: Element,
    private val layerManager: LayerManager,
    private val layerId: Int,
) : Command {
    override val description: String = "remove ${element.name}"

    override fun execute() {
        layerManager.removeElementFromLayer(element.id, layerId = layerId)
    }

    override fun revert() {
        layerManager.addElementToLayer(element = element, layerId = layerId)
    }
}

package cz.cvut.arfittingroom.draw.command.action

import android.os.Parcelable
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

class AddElementToLayer<T>(
    override val element: T,
    private val layerManager: LayerManager,
    private val layerId: UUID
) :
    Command<T> where T : Element {
    override fun execute() {
        layerManager.addElementToLayer(element = element, layerId = layerId)
    }

    override fun revert() {
        layerManager.removeElementFromLayer(elementId = element.id, layerId = layerId)
    }
}
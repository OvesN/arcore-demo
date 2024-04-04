package cz.cvut.arfittingroom.draw.command.action.element.impl

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.command.action.element.ElementCommand
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class ScaleElement(
    override val elementId: UUID,
    private val scalable: Scalable,
    private val newRadius: Float,
    private val oldRadius: Float
) : ElementCommand() {

    override fun execute() {
        scalable.scale(newRadius)
    }

    override fun revert() {
        scalable.scale(oldRadius)
    }
}

package cz.cvut.arfittingroom.draw.command.action.element.impl

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Repaintable
import cz.cvut.arfittingroom.draw.command.action.element.ElementCommand
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class RepaintElement(
    override val elementId: UUID,
    private val repaintable: Repaintable,
    private val newColor: Int,
    private val oldColor: Int
) : ElementCommand() {
    override fun execute() {
        repaintable.repaint(newColor)
    }

    override fun revert() {
        repaintable.repaint(oldColor)
    }
}
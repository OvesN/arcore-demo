package cz.cvut.arfittingroom.draw.command.action.element.impl

import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.command.action.element.ElementCommand
import java.util.UUID

class MoveElement(
    override val elementId: UUID,
    private val movable: Movable,
    private val oldX: Float,
    private val oldY: Float,
    private val newX: Float,
    private val newY: Float
) : ElementCommand() {
    override fun execute() {
        movable.move(newX, newY)
    }

    override fun revert() {
        movable.move(oldX, oldY)
    }
}
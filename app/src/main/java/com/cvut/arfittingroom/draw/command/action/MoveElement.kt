package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.model.element.Movable

/**
 * Command to move an element from one position to another
 *
 * @property movable
 * @property oldX
 * @property oldY
 * @property newX
 * @property newY
 *
 * @author Veronika Ovsyannikova
 */
class MoveElement(
    private val movable: Movable,
    private val oldX: Float,
    private val oldY: Float,
    private val newX: Float,
    private val newY: Float,
) : Command {
    override val description: String = "move element"

    override fun execute() {
        movable.move(newX, newY)
    }

    override fun revert() {
        movable.move(oldX, oldY)
    }
}

package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class MoveElement<T>(override val element: T,
                     private val oldX: Float,
                     private val oldY: Float,
                     private val newX: Float,
                     private val newY: Float) :
    Command<T> where T : Element {
    override fun execute() {
        element.move(newX, newY)
    }

    override fun revert() {
        element.move(oldX, oldY)
    }
}
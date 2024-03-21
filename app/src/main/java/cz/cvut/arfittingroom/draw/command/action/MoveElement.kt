package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class MoveElement<T>(override val element: T, private val newX: Float, private val newY: Float) :
    Command<T> where T : Element {
    override lateinit var layerId: UUID
    override fun execute(canvas: Canvas) {
        element.move(newX, newY)
    }
}
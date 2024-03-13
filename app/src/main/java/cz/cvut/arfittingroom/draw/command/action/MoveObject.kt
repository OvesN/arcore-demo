package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Movable

class MoveObject(private val movable: Movable): Command {
    override fun execute(canvas: Canvas) {
        movable.move()
    }
}
package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Rotatable

class RotateObject(private val rotatable: Rotatable): Command {
    override fun execute(canvas: Canvas) {
        rotatable.rotate()
    }
}
package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.PaintOptions
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Drawable

class DrawPath(private val drawable: Drawable): Command {
    override fun execute(canvas: Canvas) {
        drawable.draw(canvas)
    }

}
package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.Layer
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID
class DrawImage<T>(override val element: T): Command<T> where T : Element, T : Drawable {
    override fun execute(canvas: Canvas) {
        element.draw(canvas)
    }
}
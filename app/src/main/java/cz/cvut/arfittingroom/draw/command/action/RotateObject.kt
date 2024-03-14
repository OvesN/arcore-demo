package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class RotateObject<T>(override val element: T, private val rotatable: Rotatable): Command<T> where T : Element, T : Rotatable {
    override lateinit var layerId: UUID
    override fun execute(canvas: Canvas) {
        rotatable.rotate()
    }
}
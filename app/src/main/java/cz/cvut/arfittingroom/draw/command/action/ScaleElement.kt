package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class ScaleElement<T>(override val element: T,
                      private val scalable: Scalable,
                      private val factor: Float) :
    Command<T> where T : Element, T : Scalable {
    override lateinit var layerId: UUID
    override fun execute(canvas: Canvas) {
        scalable.scale(factor)
    }
}

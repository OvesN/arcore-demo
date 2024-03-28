package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class ScaleElement<T>(
    override val element: T,
    private val scaleFactor: Float
) : Command<T> where T : Element {
    override fun execute() {
        element.scale(scaleFactor)
    }

    override fun revert() {
        if (scaleFactor != 0f) {
            element.scale(1 / scaleFactor)
        }
    }
}

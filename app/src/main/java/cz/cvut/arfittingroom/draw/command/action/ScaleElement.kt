package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class ScaleElement<T>(
    override val element: T,
    private val newRadius: Float,
    private val oldRadius: Float
) : Command<T> where T : Element {
    override fun execute() {
        element.scale(newRadius)
    }

    override fun revert() {
        element.scale(oldRadius)
    }
}

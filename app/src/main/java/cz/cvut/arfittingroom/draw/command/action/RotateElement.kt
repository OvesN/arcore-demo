package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

class RotateElement<T>(override val element: T, private val rotatable: Rotatable): Command<T> where T : Element {
    override fun execute() {
        rotatable.rotate()
    }

    override fun revert() {

    }
}
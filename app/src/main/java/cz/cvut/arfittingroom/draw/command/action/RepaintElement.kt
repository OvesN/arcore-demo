package cz.cvut.arfittingroom.draw.command.action

import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Repaintable
import cz.cvut.arfittingroom.draw.model.element.Element

class RepaintElement<T>(
    override val element: T,
    private val newColor: Int,
    private val oldColor: Int
) : Command<T> where T : Element, T : Repaintable{
    override fun execute() {
        element.repaint(newColor)
    }

    override fun revert() {
        element.repaint(oldColor)
    }

}
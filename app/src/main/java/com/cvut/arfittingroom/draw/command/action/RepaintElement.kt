package com.cvut.arfittingroom.draw.command.action

import android.sax.Element
import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.command.Repaintable
import java.util.UUID

class RepaintElement(
    private val repaintable: Repaintable,
    private val newColor: Int,
    private val oldColor: Int,
    private val fill: Boolean,
    private val wasFilled: Boolean
) : Command {
    override val description: String = "change color of element"

    override fun execute() {
        repaintable.repaint(newColor, fill)
    }

    override fun revert() {
        repaintable.repaint(oldColor, wasFilled)
    }
}

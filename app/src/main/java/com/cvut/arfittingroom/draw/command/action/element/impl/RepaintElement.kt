package com.cvut.arfittingroom.draw.command.action.element.impl

import com.cvut.arfittingroom.draw.command.Repaintable
import com.cvut.arfittingroom.draw.command.action.element.ElementCommand
import java.util.UUID

class RepaintElement(
    override val elementId: UUID,
    private val repaintable: Repaintable,
    private val newColor: Int,
    private val oldColor: Int,
    private val fill: Boolean,
    private val wasFilled: Boolean
) : ElementCommand() {
    override val description: String = "change color of element"

    override fun execute() {
        repaintable.repaint(newColor, fill)
    }

    override fun revert() {
        repaintable.repaint(oldColor, wasFilled)
    }
}

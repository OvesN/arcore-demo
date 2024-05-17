package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.command.Scalable
import java.util.UUID

class ScaleElement(
    private val scalable: Scalable,
    private val newRadius: Float,
    private val oldRadius: Float,
) : Command {
    override val description: String = "scale element"

    override fun execute() {
        scalable.scale(newRadius)
    }

    override fun revert() {
        scalable.scale(oldRadius)
    }
}

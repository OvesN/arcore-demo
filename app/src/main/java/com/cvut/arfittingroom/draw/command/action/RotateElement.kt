package com.cvut.arfittingroom.draw.command.action

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.command.Rotatable
import java.util.UUID

class RotateElement(
    private val rotatable: Rotatable,
    private val newRotationAngle: Float,
    private val oldRotationAngle: Float,
) : Command {
    override val description: String = "rotate element"

    override fun execute() {
        rotatable.rotate(newRotationAngle)
    }

    override fun revert() {
        rotatable.rotate(oldRotationAngle)
    }
}

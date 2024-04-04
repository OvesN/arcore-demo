package cz.cvut.arfittingroom.draw.command.action.element.impl

import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.action.element.ElementCommand
import java.util.UUID

class RotateElement(
    override val elementId: UUID,
    private val rotatable: Rotatable,
    private val newRotationAngle: Float,
    private val oldRotationAngle: Float
) : ElementCommand(){
    override fun execute() {
        rotatable.rotate(newRotationAngle)
    }

    override fun revert() {
        rotatable.rotate(oldRotationAngle)
    }
}
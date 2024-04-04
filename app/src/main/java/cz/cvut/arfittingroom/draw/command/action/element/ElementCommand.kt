package cz.cvut.arfittingroom.draw.command.action.element

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

abstract class ElementCommand: Command {
    abstract val elementId: UUID
}
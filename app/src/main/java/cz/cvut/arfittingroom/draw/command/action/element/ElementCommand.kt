package cz.cvut.arfittingroom.draw.command.action.element

import cz.cvut.arfittingroom.draw.command.Command
import java.util.UUID

abstract class ElementCommand: Command {
    abstract val elementId: UUID
}
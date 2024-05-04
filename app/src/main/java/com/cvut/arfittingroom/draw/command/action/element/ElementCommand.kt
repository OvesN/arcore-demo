package com.cvut.arfittingroom.draw.command.action.element

import com.cvut.arfittingroom.draw.command.Command
import java.util.UUID

abstract class ElementCommand : Command {
    abstract val elementId: UUID
}

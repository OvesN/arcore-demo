package cz.cvut.arfittingroom.draw.command.action.layer

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

abstract class LayerCommand : Command {
    abstract val layerManager: LayerManager
    abstract val layerId: UUID
}
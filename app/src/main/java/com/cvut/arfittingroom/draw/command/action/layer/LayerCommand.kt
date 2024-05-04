package com.cvut.arfittingroom.draw.command.action.layer

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.draw.service.LayerManager
import java.util.UUID

abstract class LayerCommand : Command {
    abstract val layerManager: LayerManager
    abstract val layerId: UUID
}

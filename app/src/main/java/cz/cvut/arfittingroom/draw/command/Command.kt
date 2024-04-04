package cz.cvut.arfittingroom.draw.command

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.Layer
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.UUID

interface Command {
    fun execute()
    fun revert()
}
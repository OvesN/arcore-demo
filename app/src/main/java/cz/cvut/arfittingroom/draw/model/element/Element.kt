package cz.cvut.arfittingroom.draw.model.element

import cz.cvut.arfittingroom.draw.command.Drawable
import java.util.UUID

abstract class Element {
    val id: UUID = UUID.randomUUID()
    abstract fun doIntersect(x: Int, y: Int): Boolean
}
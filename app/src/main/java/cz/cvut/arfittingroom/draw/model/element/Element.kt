package cz.cvut.arfittingroom.draw.model.element

import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.Scalable
import java.util.UUID

abstract class Element : Scalable, Drawable, Movable, Rotatable {
    abstract var centerX: Float
    abstract var centerY: Float
    abstract var outerRadius: Float
    abstract var boundingBox: BoundingBox
    var rotationAngle = 0f
    protected var originalRotationAngle = 0f

    val id: UUID = UUID.randomUUID()
    var isSelected: Boolean = false

    abstract fun doIntersect(x: Float, y: Float): Boolean

    protected fun createBoundingBox(): BoundingBox =
        BoundingBox(centerX, centerY, outerRadius)
}
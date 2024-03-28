package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Color
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.path.DrawablePath
import java.util.UUID

abstract class Element : Scalable, Drawable, Movable, Rotatable {
    abstract var centerX: Float
    abstract var centerY: Float
    abstract var outerRadius: Float

    val id: UUID = UUID.randomUUID()
    var isSelected: Boolean = false

    protected val boundingBoxPaint: Paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    abstract fun doIntersect(x: Float, y: Float): Boolean
    abstract fun createBoundingBox(): DrawablePath

}
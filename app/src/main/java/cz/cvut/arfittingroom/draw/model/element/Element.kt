package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Color
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.path.DrawablePath
import java.util.UUID

abstract class Element {
    val id: UUID = UUID.randomUUID()
    var isSelected: Boolean = false
    private var wasSelected: Boolean = false
    protected val boundingBoxPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    abstract fun doIntersect(x: Float, y: Float): Boolean

    fun toggleSelected() {
        isSelected = !wasSelected
    }

    fun deselect() {
        wasSelected = isSelected
        isSelected = false
    }
}
package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.strategy.impl.RectanglePathCreationStrategy
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.model.Coordinates

class BoundingBox(
    var centerX: Float,
    var centerY: Float,
    var outerRadius: Float,
) : Drawable {

    private val paint: Paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    var topRightCornerCoor = Coordinates(0f, 0f)
    var topLeftCornerCoor = Coordinates(0f, 0f)
    var bottomRightCornerCoor = Coordinates(0f, 0f)
    var bottomLeftCornerCoor = Coordinates(0f, 0f)

    var elementPath: DrawablePath

    init {
        elementPath = createPath()
    }

    private fun createPath(): DrawablePath =
        RectanglePathCreationStrategy().createPath(
            centerX = centerX,
            centerY = centerY,
            outerRadius = outerRadius
        )

    override fun draw(canvas: Canvas) {
        canvas.drawPath(createPath(), paint)
    }
}
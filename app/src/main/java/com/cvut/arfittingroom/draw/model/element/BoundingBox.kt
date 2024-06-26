package com.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.cvut.arfittingroom.draw.path.DrawablePath
import com.cvut.arfittingroom.model.Coordinates

/**
 * Rectangular boundary around the element, helps to detect intersections
 * with the user's finger and is drawn when the element is selected
 *
 * @property centerX
 * @property centerY
 * @property outerRadius
 *
 * @author Veronika Ovsyannikova
 */
class BoundingBox(
    var centerX: Float,
    var centerY: Float,
    var outerRadius: Float,
) : Drawable {
    private val paint: Paint =
        Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect
        }
    var topRightCornerCoor = Coordinates(0f, 0f)
    var topLeftCornerCoor = Coordinates(0f, 0f)
    var bottomRightCornerCoor = Coordinates(0f, 0f)
    var bottomLeftCornerCoor = Coordinates(0f, 0f)
    private var elementPath: DrawablePath = createPath()
    var rectF: RectF = RectF()

    init {
        elementPath.computeBounds(rectF, true)
    }

    private fun createPath(): DrawablePath {
        val path = DrawablePath()

        // Start at the top-left corner
        topLeftCornerCoor = Coordinates(centerX - outerRadius, centerY - outerRadius)
        path.moveTo(topLeftCornerCoor.x, topLeftCornerCoor.y)

        // Draw line to the top-right corner
        topRightCornerCoor = Coordinates(centerX + outerRadius, centerY - outerRadius)
        path.lineTo(topRightCornerCoor.x, topLeftCornerCoor.y)

        // Draw line to the bottom-right corner
        bottomRightCornerCoor = Coordinates(centerX + outerRadius, centerY + outerRadius)
        path.lineTo(bottomRightCornerCoor.x, bottomRightCornerCoor.y)

        // Draw line to the bottom-left corner
        bottomLeftCornerCoor = Coordinates(centerX - outerRadius, centerY + outerRadius)
        path.lineTo(bottomLeftCornerCoor.x, bottomLeftCornerCoor.y)

        // Close the path back to the top-left corner
        path.lineTo(centerX - outerRadius, centerY - outerRadius)

        path.close()
        return path
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(createPath(), paint)
    }

    fun contains(
        x: Float,
        y: Float,
    ) = RectF(
        rectF.left - 10,
        rectF.top - 10,
        rectF.right + 10,
        rectF.bottom + 10,
    ).contains(x, y)
}

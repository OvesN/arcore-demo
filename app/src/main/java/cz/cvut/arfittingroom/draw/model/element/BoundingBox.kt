package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.androidCanvas.IAndroidCanvasHelper
import com.chillingvan.canvasgl.glcanvas.GLPaint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.strategy.impl.RectanglePathCreationStrategy
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.model.Coordinates
import cz.cvut.arfittingroom.utils.drawPath

class BoundingBox(
    var centerX: Float,
    var centerY: Float,
    var outerRadius: Float,
) : Drawable {

    private val paint: GLPaint = GLPaint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        lineWidth = 2f
    }

    var topRightCornerCoor = Coordinates(0f, 0f)
    var topLeftCornerCoor = Coordinates(0f, 0f)
    var bottomRightCornerCoor = Coordinates(0f, 0f)
    var bottomLeftCornerCoor = Coordinates(0f, 0f)

    var elementPath: DrawablePath = createPath()
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

    override fun draw(canvas: ICanvasGL) {
        canvas.drawPath(createPath(), paint)
    }
}
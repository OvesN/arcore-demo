package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.Element
import kotlin.math.cos
import kotlin.math.sin

class Star(
    private var centerX: Float,
    private var centerY: Float,
    private var outerRadius: Float,
    private var paint: Paint
) : Element(), Drawable {
    private var starPath: DrawablePath
    private var boundingBoxPath: DrawablePath

    init {
        starPath = createPath()
        boundingBoxPath = createBoundingBox()
    }

    private fun createPath(): DrawablePath {
        val section = 2.0 * Math.PI / 5
        val path = DrawablePath()
        val innerRadius = outerRadius / 3
        val startAngle = -Math.PI / 2 // Start angle set to -90 degrees

        path.reset()
        path.moveTo(
            (centerX + outerRadius * cos(startAngle)).toFloat(),
            (centerY + outerRadius * sin(startAngle)).toFloat()
        )
        path.lineTo(
            (centerX + innerRadius * cos(startAngle + section / 2.0)).toFloat(),
            (centerY + innerRadius * sin(startAngle + section / 2.0)).toFloat()
        )

        for (i in 1 until 5) {
            path.lineTo(
                (centerX + outerRadius * cos(startAngle + section * i)).toFloat(),
                (centerY + outerRadius * sin(startAngle + section * i)).toFloat()
            )
            path.lineTo(
                (centerX + innerRadius * cos(startAngle + section * i + section / 2.0)).toFloat(),
                (centerY + innerRadius * sin(startAngle + section * i + section / 2.0)).toFloat()
            )
        }

        path.close()
        return path
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        val rectF = RectF()
        boundingBoxPath.computeBounds(rectF, true)
        return rectF.contains(x, y)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(createPath(), paint)

        // If element is selected, draw  bounding box around it
        if (isSelected) {
            canvas.drawPath(createBoundingBox(), boundingBoxPaint)
        }
    }

    private fun createBoundingBox(): DrawablePath {
        val path = DrawablePath()

        // Start at the top-left corner of the bounding box
        path.moveTo(centerX - outerRadius, centerY - outerRadius)

        // Draw line to the top-right corner
        path.lineTo(centerX + outerRadius, centerY - outerRadius)

        // Draw line to the bottom-right corner
        path.lineTo(centerX + outerRadius, centerY + outerRadius)

        // Draw line to the bottom-left corner
        path.lineTo(centerX - outerRadius, centerY + outerRadius)

        // Close the path back to the top-left corner
        path.lineTo(centerX - outerRadius, centerY - outerRadius)

        path.close()
        return path
    }

}
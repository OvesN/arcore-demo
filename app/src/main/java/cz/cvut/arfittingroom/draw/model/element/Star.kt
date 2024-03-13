package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.command.Drawable
import kotlin.math.cos
import kotlin.math.sin

class Star(
    private val centerX: Float,
    private val centerY: Float,
    private val outerRadius: Float,
    private val paint: Paint
) : Drawable {
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

    override fun draw(canvas: Canvas) {
        canvas.drawPath(createPath(), paint)
    }
}
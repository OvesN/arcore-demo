package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.element.Figure
import kotlin.math.cos
import kotlin.math.sin

class Star(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    private var paint: Paint
) : Figure() {
    override var elementPath: DrawablePath
    override var boundingBoxPath: DrawablePath
    override var originalRadius: Float
    override var originalCenterX: Float
    override var originalCenterY: Float

    init {
        elementPath = createPath()
        boundingBoxPath = createBoundingBox()
        originalRadius = outerRadius
        originalCenterX = centerX
        originalCenterY = centerY
    }

    override fun createPath(): DrawablePath {
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

        // If element is selected, draw  bounding box around it
        if (isSelected) {
            canvas.drawPath(createBoundingBox(), boundingBoxPaint)
        }
    }

}
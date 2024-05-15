package com.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import com.cvut.arfittingroom.draw.command.Repaintable
import com.cvut.arfittingroom.draw.model.element.BoundingBox
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.path.DrawablePath
import java.util.UUID

class Stamp(
    override val id: UUID = UUID.randomUUID(),
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    private val pathCreationStrategy: PathCreationStrategy,
    override val paint: Paint,
    override var rotationAngle: Float = 0f,
) : Element(), Repaintable {
    override val name: String = pathCreationStrategy.name
    override var originalRadius: Float = outerRadius
    override var originalCenterX: Float = centerX
    override var originalCenterY: Float = centerY
    override var boundingBox: BoundingBox = createBoundingBox()

    private fun createPath(): DrawablePath = pathCreationStrategy.createPath(centerX, centerY, outerRadius)

    override fun drawSpecific(canvas: Canvas) {
        canvas.save()

        // Rotate the canvas around the element's center
        canvas.rotate(rotationAngle, centerX, centerY)

        // Draw the element's path
        canvas.drawPath(createPath(), paint)

        canvas.restore()
    }

    override fun repaint(newColor: Int, fill: Boolean) {
        paint.color = newColor
        if (fill) {
            paint.style = Paint.Style.FILL
        }
        else {
            paint.style = Paint.Style.STROKE
        }
    }
}

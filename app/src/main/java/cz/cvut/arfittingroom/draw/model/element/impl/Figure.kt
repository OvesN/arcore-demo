package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.glcanvas.GLPaint
import cz.cvut.arfittingroom.draw.command.Repaintable
import cz.cvut.arfittingroom.draw.model.PaintOptions
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.utils.drawPath
import kotlin.math.max

class Figure(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    private val pathCreationStrategy: PathCreationStrategy,
    private var paint: GLPaint
) : Element(), Repaintable {

    override var originalRadius: Float = outerRadius
    override var originalCenterX: Float = centerX
    override var originalCenterY: Float = centerY
    override var boundingBox: BoundingBox = createBoundingBox()

    private fun createPath(): DrawablePath = pathCreationStrategy.createPath(centerX, centerY, outerRadius)

    override fun drawSpecific(canvas: ICanvasGL) {
        canvas.save()

        // Rotate the canvas around the element's center
        canvas.rotate(rotationAngle, centerX, centerY)

        // Draw the element's path
        canvas.drawPath(createPath(), paint)

        canvas.restore()

    }

    override fun repaint(newPaint: PaintOptions) {
        TODO("Not yet implemented")
    }

}
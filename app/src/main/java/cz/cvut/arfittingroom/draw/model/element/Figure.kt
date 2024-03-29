package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import cz.cvut.arfittingroom.draw.command.ColorChangeable
import cz.cvut.arfittingroom.draw.model.element.impl.Rectangle
import cz.cvut.arfittingroom.draw.path.DrawablePath
import kotlin.math.max

abstract class Figure : Element(), ColorChangeable {
    abstract override var centerX: Float
    abstract override var centerY: Float
    abstract override var outerRadius: Float
    abstract override var boundingBox: BoundingBox

    abstract var elementPath: DrawablePath

    abstract var originalCenterX: Float
    abstract var originalCenterY: Float
    abstract var originalRadius: Float

    abstract var paint: Paint

    abstract fun createPath(): DrawablePath

    override fun scale(newRadius: Float) {
        outerRadius = max(newRadius, 1f)

        elementPath = createPath()
        boundingBox = createBoundingBox()
    }

    // Scale while scaling gesture
    override fun continuousScale(factor: Float) {
        val newRadius = max(factor * originalRadius, 1f)

        outerRadius = newRadius
        elementPath = createPath()
        boundingBox = createBoundingBox()
    }

    // End of the scale gesture by the user
    // Returns radius to the original one so scale action can be applied correctly
    override fun endContinuousScale() {
        outerRadius = originalRadius
    }

    override fun move(x: Float, y: Float) {
        centerX = x
        centerY = y

        elementPath = createPath()
        boundingBox = createBoundingBox()
    }

    // End of the move gesture by the user
    // Returns center x and y to the original values so move action can be applied correctly
    override fun endContinuousMove() {
        centerX = originalCenterX
        centerY = originalCenterY
    }

    override fun rotate() {
        TODO("Not yet implemented")
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        val rectF = RectF()
        boundingBox.elementPath.computeBounds(rectF, true)
        return rectF.contains(x, y)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(createPath(), paint)

        // If element is selected, draw  bounding box around it
        if (isSelected) {
            boundingBox.draw(canvas)
        }
    }
}
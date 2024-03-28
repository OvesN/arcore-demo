package cz.cvut.arfittingroom.draw.model.element

import android.graphics.RectF
import cz.cvut.arfittingroom.draw.path.DrawablePath

abstract class Figure : Element() {
    abstract var centerX: Float
    abstract var centerY: Float
    abstract var outerRadius: Float

    abstract var elementPath: DrawablePath
    abstract var boundingBoxPath: DrawablePath

    abstract var originalCenterX: Float
    abstract var originalCenterY: Float
    abstract var originalRadius: Float

    abstract fun createPath(): DrawablePath

    override fun createBoundingBox(): DrawablePath {
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

    override fun scale(factor: Float) {
        outerRadius *= factor

        elementPath = createPath()
        boundingBoxPath = createBoundingBox()
    }

    // Scale while scaling gesture
    override fun continuousScale(factor: Float) {
        outerRadius = factor * originalRadius

        elementPath = createPath()
        boundingBoxPath = createBoundingBox()
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
        boundingBoxPath = createBoundingBox()
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
        boundingBoxPath.computeBounds(rectF, true)
        return rectF.contains(x, y)
    }
}
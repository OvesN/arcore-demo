package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import cz.cvut.arfittingroom.draw.command.Repaintable
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import kotlin.math.max

private const val PROXIMITY_THRESHOLD = 60f // pixels

class Curve(
    private var path: DrawablePath,
    override val paint: Paint
) : Element(), Repaintable {

    override var centerX: Float = 0f
    override var centerY: Float = 0f
    override var outerRadius: Float = 0f

    override var boundingBox: BoundingBox

    //For continuous scaling so gradually changes will be applied to the original value
    override var originalCenterX: Float
    override var originalCenterY: Float
    override var originalRadius: Float

    private var xDiff: Float = 0f  // No translation by default
    private var yDiff: Float = 0f  // No translation by default
    private var radiusDiff: Float = 1f  // No scaling by default

    init {
        boundingBox = updateBoundingBoxAndCenter()

        originalCenterX = centerX
        originalCenterY = centerY
        originalRadius = outerRadius
    }

    override fun drawSpecific(canvas: Canvas) {
        val transformedPath = DrawablePath()
        path.transform(createTransformationMatrix(), transformedPath)

        canvas.drawPath(transformedPath, paint)
    }

    // These functions are overriden because
    // we want to know the diff between old value and new value to create transformation matrix
    override fun move(x: Float, y: Float) {
        centerX = x
        centerY = y

        xDiff = centerX - originalCenterX
        yDiff = centerY - originalCenterY
    }

    override fun endContinuousMove() {
        centerX = originalCenterX
        centerY = originalCenterY
    }

    // Scaling function
    override fun scale(newRadius: Float) {
        outerRadius = max(newRadius, 1f)

        radiusDiff = outerRadius / originalRadius
    }

    override fun scaleContinuously(factor: Float) {
        super.scaleContinuously(factor)

        radiusDiff = outerRadius / originalRadius
    }

    override fun endContinuousScale() {
        outerRadius = originalRadius
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        if (!boundingBox.rectF.contains(x, y)) {
            return false
        }

        val inverseMatrix = Matrix()
        createTransformationMatrix().invert(inverseMatrix)

        val point = floatArrayOf(x, y)

        inverseMatrix.mapPoints(point)

        val pm = PathMeasure(path, false)
        val pathLength = pm.length
        val pathCoords = FloatArray(2) // Holds coordinates as [x, y]

        var distance = 0f
        while (distance < pathLength) {
            // Get the coordinates at the current distance
            pm.getPosTan(distance, pathCoords, null)

            // Calculate the distance from the point to the current path segment point
            val dx = pathCoords[0] - point[0]
            val dy = pathCoords[1] - point[1]
            if (dx * dx + dy * dy <= PROXIMITY_THRESHOLD * PROXIMITY_THRESHOLD) {
                return true
            }

            distance += 1f
        }

        return false
    }

    override fun repaint(newColor: Int) {
        paint.color = newColor
    }

    private fun createTransformationMatrix(): Matrix {
        val matrix = Matrix()

        matrix.postRotate(rotationAngle, originalCenterX, originalCenterY)
        matrix.postScale(radiusDiff, radiusDiff, originalCenterX, originalCenterY)
        matrix.postTranslate(xDiff, yDiff)
        return matrix
    }

    private fun updateBoundingBoxAndCenter():BoundingBox {
        val bounds = RectF()
        path.computeBounds(bounds, true)

        centerX = bounds.centerX()
        centerY = bounds.centerY()
        outerRadius = max(bounds.width(), bounds.height()) / 2

        return createBoundingBox()
    }
}
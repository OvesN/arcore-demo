package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.RectF
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import kotlin.math.max

private const val PROXIMITY_THRESHOLD = 20f // pixels
class Curve(
    private var path: DrawablePath,
    private var paint: Paint
) : Element() {

    override var centerX: Float
    override var centerY: Float
    override var outerRadius: Float

    override var boundingBox: BoundingBox

    override var originalCenterX: Float
    override var originalCenterY: Float
    override var originalRadius: Float

    init {
        val rect = RectF()
        path.computeBounds(rect, true)

        centerX = rect.centerX()
        centerY = rect.centerY()
        outerRadius = max(rect.width(), rect.height()) / 2

        boundingBox = createBoundingBox()

        originalCenterX = centerX
        originalCenterY = centerY
        originalRadius = outerRadius
    }


    private fun createPath(): DrawablePath = path

    override fun drawSpecific(canvas: Canvas) {
        val matrix = Matrix()
        // Apply rotation
        matrix.postRotate(rotationAngle, centerX, centerY)
        // Apply scaling
        matrix.postScale(outerRadius / originalRadius, outerRadius / originalRadius, centerX, centerY)

        val transformedPath = DrawablePath()
        path.transform(matrix, transformedPath)

        canvas.drawPath(transformedPath, paint)

        if (isSelected) {
            boundingBox = createBoundingBox()
            boundingBox.draw(canvas)
        }
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        if (!boundingBox.rectF.contains(x, y)) {
            return false
        }

        val pm = PathMeasure(path, false)
        val pathLength = pm.length
        val pathCoords = FloatArray(2) // Holds coordinates as [x, y]

        var distance = 0f
        while (distance < pathLength) {
            // Get the coordinates at the current distance
            pm.getPosTan(distance, pathCoords, null)

            // Calculate the distance from the point to the current path segment point
            val dx = pathCoords[0] - x
            val dy = pathCoords[1] - y
            if (dx * dx + dy * dy <= PROXIMITY_THRESHOLD * PROXIMITY_THRESHOLD) {
                return true
            }

            distance += 1f
        }

        return false

    }
}
package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.RectF
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.glcanvas.GLPaint
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.utils.drawPath
import kotlin.math.max

private const val PROXIMITY_THRESHOLD = 40f // pixels
class Curve(
    private var path: DrawablePath,
    private var paint: GLPaint
) : Element() {

    override var centerX: Float
    override var centerY: Float
    override var outerRadius: Float

    override var boundingBox: BoundingBox

    //For continuous scaling so gradually changes will be applied to the original value
    override var originalCenterX: Float
    override var originalCenterY: Float
    override var originalRadius: Float

    private var xDiff: Float = 0f  // No translation by default
    private var yDiff: Float = 0f  // No translation by default
    private var radiusDiff: Float = 1f  // No scaling by default

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

    override fun drawSpecific(canvas: ICanvasGL) {
        val matrix = Matrix()

        matrix.postRotate(rotationAngle, centerX, centerY)
        matrix.postScale(radiusDiff, radiusDiff, centerX, centerY)
        matrix.postTranslate(xDiff, yDiff)

        val transformedPath = DrawablePath()
        path.transform(matrix, transformedPath)
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
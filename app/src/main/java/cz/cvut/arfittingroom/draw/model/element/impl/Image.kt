package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import kotlin.math.max

private const val TRANSPARENT_CODE = 0xFF000000

class Image(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    val resourceId: Int,
    private val bitmap: Bitmap,
) : Element() {

    override var boundingBox: BoundingBox = createBoundingBox()
    override var originalRadius: Float = outerRadius
    override var originalCenterX: Float = centerX
    override var originalCenterY: Float = centerY

    override fun drawSpecific(canvas: Canvas) {
        canvas.drawBitmap(bitmap, createTransformationMatrix(), null)
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        if (!boundingBox.rectF.contains(x, y)) {
            return false
        }

        // Create an inverse matrix
        val inverseMatrix = Matrix()
        createTransformationMatrix().invert(inverseMatrix)

        // Apply the inverse matrix to the (x, y) coordinates
        val points = floatArrayOf(x, y)
        inverseMatrix.mapPoints(points)

        // Check if the point is within bitmap bounds
        if (points[0] < 0 || points[0] >= bitmap.width || points[1] < 0 || points[1] >= bitmap.height) {
            return false
        }

        // Check the alpha value of the corresponding pixel in the bitmap
        val pixel = bitmap.getPixel(points[0].toInt(), points[1].toInt())
        return (pixel and TRANSPARENT_CODE.toInt()) != 0 // Check if alpha is not 0 (transparent)
    }

    private fun createTransformationMatrix(): Matrix {
        // Calculate the scale factor to fit the bitmap within the outerRadius
        val scaleFactor = (outerRadius * 2) / max(bitmap.width, bitmap.height)

        return Matrix().apply {
            postScale(scaleFactor, scaleFactor)

            // Rotate the bitmap around its center
            postRotate(
                rotationAngle,
                bitmap.width / 2f * scaleFactor,
                bitmap.height / 2f * scaleFactor
            )

            // Translate the bitmap to draw it at the specified center (centerX, centerY)
            // We adjust the translation to ensure the bitmap's center aligns with the (centerX, centerY)
            postTranslate(
                centerX - bitmap.width / 2f * scaleFactor,
                centerY - bitmap.height / 2f * scaleFactor
            )
        }
    }

}
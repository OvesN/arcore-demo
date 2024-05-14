package com.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.cvut.arfittingroom.draw.model.element.BoundingBox
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.model.TRANSPARENT_CODE
import java.util.UUID
import kotlin.math.max

class Image(
    override val id: UUID = UUID.randomUUID(),
    val resourceRef: String,
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    override var rotationAngle: Float = 0f,
) : Element() {
    override val name: String = "image"
    override var boundingBox: BoundingBox = createBoundingBox()
    override var originalRadius: Float = outerRadius
    override var originalCenterX: Float = centerX
    override var originalCenterY: Float = centerY
    private var transformationMatrix: Matrix = Matrix()
    var bitmap: Bitmap? = null

    override fun drawSpecific(canvas: Canvas) {
        bitmap?.let {
            transformationMatrix = createTransformationMatrix(it)
            canvas.drawBitmap(it, transformationMatrix, null)
        }
    }

    override fun doesIntersect(
        x: Float,
        y: Float,
    ): Boolean {
        if (!boundingBox.rectF.contains(x, y)) {
            return false
        }

        // Create an inverse matrix
        val inverseMatrix = Matrix()
        transformationMatrix.invert(inverseMatrix)

        // Apply the inverse matrix to the (x, y) coordinates
        val points = floatArrayOf(x, y)
        inverseMatrix.mapPoints(points)

        // Check if the point is within bitmap bounds
        bitmap?.let {
            if (points[0] < 0 || points[0] >= it.width || points[1] < 0 || points[1] >= it.height) {
                return false
            }

            // Check the alpha value of the corresponding pixel in the bitmap
            val pixel = it.getPixel(points[0].toInt(), points[1].toInt())
            return (pixel and TRANSPARENT_CODE.toInt()) != 0  // Check if alpha is not 0 (transparent)
        }
        return false
    }

    private fun createTransformationMatrix(bitmap: Bitmap): Matrix {
        // Calculate the scale factor to fit the bitmap within the outerRadius
        val scaleFactor = (outerRadius * 2) / max(bitmap.width, bitmap.height)

        return Matrix().apply {
            postScale(scaleFactor, scaleFactor)

            // Rotate the bitmap around its center
            postRotate(
                rotationAngle,
                bitmap.width / 2f * scaleFactor,
                bitmap.height / 2f * scaleFactor,
            )

            // Translate the bitmap to draw it at the specified center (centerX, centerY)
            // We adjust the translation to ensure the bitmap center aligns with the (centerX, centerY)
            postTranslate(
                centerX - bitmap.width / 2f * scaleFactor,
                centerY - bitmap.height / 2f * scaleFactor,
            )
        }
    }
}

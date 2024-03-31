package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import kotlin.math.max


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

    override fun draw(canvas: Canvas) {
        // Calculate the scale factor to fit the bitmap within the outerRadius
        val scaleFactor = (outerRadius * 2) / max(bitmap.width, bitmap.height)

        val matrix = Matrix().apply {
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

        canvas.drawBitmap(bitmap, matrix, null)

        // If element is selected, draw  bounding box around it
        if (isSelected) {
            boundingBox = createBoundingBox()
            boundingBox.draw(canvas)
        }
    }

}
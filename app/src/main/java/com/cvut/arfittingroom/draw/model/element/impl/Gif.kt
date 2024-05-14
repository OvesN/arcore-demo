package com.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Log
import com.cvut.arfittingroom.draw.model.element.BoundingBox
import com.cvut.arfittingroom.draw.model.element.Element
import pl.droidsonroids.gif.GifDrawable
import java.util.UUID
import kotlin.math.max

class Gif(
    override val id: UUID = UUID.randomUUID(),
    val resourceRef: String,
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    override var rotationAngle: Float = 0f,
) : Element() {
    override val name: String = "gif"
    override var boundingBox: BoundingBox = createBoundingBox()
    override var originalRadius: Float = outerRadius
    override var originalCenterX: Float = centerX
    override var originalCenterY: Float = centerY
    private var transformationMatrix: Matrix = Matrix()
    private var firstFrameBitmap: Bitmap? = null
    var shouldDrawNextFrame = false
    var increaseFrameIndexEachDraw = false
    var currentFrameIndex = 0
    var gifDrawable: GifDrawable? = null

    fun setDrawable(gifDrawable: GifDrawable) {
        this.gifDrawable = gifDrawable
        firstFrameBitmap = gifDrawable.seekToFrameAndGet(0)
    }

    override fun drawSpecific(canvas: Canvas) {
        gifDrawable?.let {
            if (increaseFrameIndexEachDraw) {
                currentFrameIndex++
            }
            if (currentFrameIndex >= it.numberOfFrames) {
                currentFrameIndex = 0
            }

            transformationMatrix = createTransformationMatrix(it)
            val bitmapToDraw =
                if (shouldDrawNextFrame) it.seekToFrameAndGet(currentFrameIndex) else firstFrameBitmap

            bitmapToDraw?.let { bitmap -> canvas.drawBitmap(bitmap, transformationMatrix, null) }
            if (shouldDrawNextFrame) {
                Log.println(Log.INFO, null, "frame $currentFrameIndex is drawn")
            }
        }

    }

    override fun setSelected(isSelected: Boolean) {
        super.setSelected(isSelected)
        shouldDrawNextFrame = isSelected
        currentFrameIndex = 0
    }

    private fun createTransformationMatrix(gifDrawable: GifDrawable): Matrix {
        val width = gifDrawable.currentFrame.width
        val height = gifDrawable.currentFrame.height

        // Calculate the scale factor to fit the bitmap within the outerRadius
        val scaleFactor = (outerRadius * 2) / max(width, height)

        return Matrix().apply {
            postScale(scaleFactor, scaleFactor)

            // Rotate the bitmap around its center
            postRotate(
                rotationAngle,
                width / 2f * scaleFactor,
                height / 2f * scaleFactor,
            )

            // Translate the bitmap to draw it at the specified center (centerX, centerY)
            // We adjust the translation to ensure the bitmap center aligns with the (centerX, centerY)
            postTranslate(
                centerX - width / 2f * scaleFactor,
                centerY - height / 2f * scaleFactor,
            )
        }
    }
}

package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Log
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.model.TRANSPARENT_CODE
import pl.droidsonroids.gif.GifDrawable
import kotlin.math.max

class Gif(
    private val gifId: Int,
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
) : Element() {
    override val name: String = "gif"
    override var boundingBox: BoundingBox = createBoundingBox()
    override var originalRadius: Float = outerRadius

    override var originalCenterX: Float = centerX
    override var originalCenterY: Float = centerY
    private var transformationMatrix: Matrix = Matrix()

    lateinit var gifDrawable: GifDrawable
    private var firstFrameBitmap: Bitmap? = null
    var shouldDrawNextFrame = false
    var increaseFrameIndexEachDraw = false

    var currentFrameIndex = 0

    fun setDrawable(gifDrawable: GifDrawable) {
        this.gifDrawable = gifDrawable
        firstFrameBitmap = gifDrawable.seekToFrameAndGet(0)
    }

    override fun drawSpecific(canvas: Canvas) {
        if (increaseFrameIndexEachDraw) {
            currentFrameIndex++
        }
        if (currentFrameIndex >= gifDrawable.numberOfFrames) {
            currentFrameIndex = 0
        }

        transformationMatrix = createTransformationMatrix()
        val bitmapToDraw =
            if (shouldDrawNextFrame) gifDrawable.seekToFrameAndGet(currentFrameIndex) else firstFrameBitmap

        bitmapToDraw?.let { canvas.drawBitmap(it, transformationMatrix, null) }
        if (shouldDrawNextFrame) {
            Log.println(Log.INFO, null, "frame $currentFrameIndex is drawn")
        }
    }

    override fun setSelected(isSelected: Boolean) {
        super.setSelected(isSelected)
        shouldDrawNextFrame = isSelected
        currentFrameIndex = 0
    }

    //TODO resolve later copy paste from image
    override fun doIntersect(x: Float, y: Float): Boolean {
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
        if (points[0] < 0 || points[0] >= gifDrawable.currentFrame.width || points[1] < 0 || points[1] >= gifDrawable.currentFrame.height) {
            return false
        }

        // Check the alpha value of the corresponding pixel in the bitmap
        val pixel = gifDrawable.currentFrame.getPixel(points[0].toInt(), points[1].toInt())
        return (pixel and TRANSPARENT_CODE.toInt()) != 0 // Check if alpha is not 0 (transparent)
    }

    private fun createTransformationMatrix(): Matrix {
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
                height / 2f * scaleFactor
            )

            // Translate the bitmap to draw it at the specified center (centerX, centerY)
            // We adjust the translation to ensure the bitmap center aligns with the (centerX, centerY)
            postTranslate(
                centerX - width / 2f * scaleFactor,
                centerY - height / 2f * scaleFactor
            )
        }
    }
}
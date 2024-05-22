package com.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.RectF
import com.cvut.arfittingroom.draw.model.element.Repaintable
import com.cvut.arfittingroom.draw.model.element.BoundingBox
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.path.DrawablePath
import com.cvut.arfittingroom.draw.service.TexturedBrushDrawer
import com.cvut.arfittingroom.utils.BitmapUtil
import java.util.UUID
import kotlin.math.max

private const val PROXIMITY_THRESHOLD = 60f

/**
 * Drawable element that uses [path] to draw poly-lines on the canvas
 *
 * @property id
 * @property path
 * @property paint
 * @property rotationAngle
 * @property bitmapTexture The bitmap of the texture for textured brushes
 * @property strokeTextureRef The reference to the texture image stored
 * in Firebase Storage for textured brushes
 * @property blurRadius
 * @property blurType
 * @property centerX
 * @property centerY
 * @property outerRadius
 * @property xdiff
 * @property ydiff
 * @property radiusDiff
 *
 * @author Veronika Ovsyannikova
 */
class Curve(
    override val id: UUID = UUID.randomUUID(),
    var path: DrawablePath,
    override val paint: Paint,
    override var rotationAngle: Float = 0f,
    var bitmapTexture: Bitmap? = null,
    var strokeTextureRef: String = "",
    var blurRadius: Float = 0f,
    var blurType: Blur = Blur.NORMAL,
    override var centerX: Float = 0f,
    override var centerY: Float = 0f,
    override var outerRadius: Float = 0f,
    var xdiff: Float = 0f,  // No translation by default
    var ydiff: Float = 0f,  // No translation by default
    var radiusDiff: Float = 1f,
) : Element(), Repaintable {
    override val name: String = "Line"
    override var boundingBox: BoundingBox

    // For continuous scaling so gradually changes will be applied to the original value
    override var originalCenterX: Float = 0f
    override var originalCenterY: Float = 0f
    override var originalRadius: Float = 0f
    private var originalStrokeWidth: Float = paint.strokeWidth

    private var scaledTextureBitmap: Bitmap? = null

    init {
        if (blurRadius != 0f) {
            paint.apply { maskFilter = BlurMaskFilter(blurRadius, blurType) }
        }

        boundingBox = updateBoundingBoxAndCenterDimensions()

        bitmapTexture?.let {
            setTextureBitmap(it)
        }
    }

    fun setTextureBitmap(bitmap: Bitmap) {
        bitmapTexture = bitmap
        updateTextureBitmap()
    }


    override fun drawSpecific(canvas: Canvas) {
        val transformedPath = DrawablePath()
        path.transform(createTransformationMatrix(), transformedPath)

        if (scaledTextureBitmap != null) {
            scaledTextureBitmap?.let {
                TexturedBrushDrawer.draw(
                    canvas,
                    transformedPath,
                    it,
                    paint.strokeWidth
                )
            }
        } else {
            canvas.drawPath(transformedPath, paint)
        }
    }

    // These functions are overriden because
    // we want to know the diff between old value and new value to create transformation matrix
    override fun move(
        x: Float,
        y: Float,
    ) {
        centerX = x
        centerY = y

        xdiff = centerX - originalCenterX
        ydiff = centerY - originalCenterY
    }

    override fun endContinuousMove() {
        centerX = originalCenterX
        centerY = originalCenterY
    }

    override fun scale(newRadius: Float) {
        outerRadius = max(newRadius, 10f)
        radiusDiff = max(outerRadius / originalRadius, 0.1f)
        updateStrokeWidth()
    }

    override fun scaleContinuously(factor: Float) {
        super.scaleContinuously(factor)
        radiusDiff = max(outerRadius / originalRadius, 0.1f)
        updateStrokeWidth()
    }

    override fun endContinuousScale() {
        outerRadius = originalRadius
        updateStrokeWidth()
    }

    override fun doesIntersect(
        x: Float,
        y: Float,
    ): Boolean {
        if (!boundingBox.rectF.contains(x, y)) {
            return false
        }

        val inverseMatrix = Matrix()
        createTransformationMatrix().invert(inverseMatrix)

        val point = floatArrayOf(x, y)

        inverseMatrix.mapPoints(point)

        val pm = PathMeasure(path, false)
        val pathLength = pm.length
        val pathCoords = FloatArray(2)

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

    override fun repaint(newColor: Int, fill: Boolean) {
        paint.color = newColor
        updateTextureBitmap()
    }

    private fun createTransformationMatrix(): Matrix {
        val matrix = Matrix()

        matrix.postRotate(rotationAngle, originalCenterX, originalCenterY)
        matrix.postScale(radiusDiff, radiusDiff, originalCenterX, originalCenterY)
        matrix.postTranslate(xdiff, ydiff)
        return matrix
    }

    private fun updateBoundingBoxAndCenterDimensions(): BoundingBox {
        val bounds = RectF()
        path.computeBounds(bounds, true)

        centerX = bounds.centerX()
        centerY = bounds.centerY()

        val boundsCenter = max(bounds.width(), bounds.height()) / 2

        if (boundsCenter > outerRadius) {
            outerRadius = boundsCenter
        }

        originalCenterX = centerX
        originalCenterY = centerY
        originalRadius = outerRadius

        return createBoundingBox()
    }

    private fun updateStrokeWidth() {
        paint.strokeWidth = originalStrokeWidth * radiusDiff

        updateTextureBitmap()
    }

    private fun updateTextureBitmap() {
        scaledTextureBitmap = bitmapTexture?.let {
            Bitmap.createScaledBitmap(
                it,
                paint.strokeWidth.toInt(), paint.strokeWidth.toInt(), true
            )
        }
        scaledTextureBitmap?.let {
            BitmapUtil.replaceNonTransparentPixels(
                it, (paint.alpha shl 24) or (paint.color and 0xFFFFFF)
            )
        }
    }
}

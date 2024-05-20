package com.cvut.arfittingroom.draw.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import com.cvut.arfittingroom.utils.BitmapUtil

import kotlin.math.PI
import kotlin.math.atan2

/**
 * Bitmap drawer for textured brushes
 *
 * @author Veronika Ovsyannikova
 */
object TexturedBrushDrawer {
    var originalBitmap: Bitmap? = null
    private var scaledBitmap: Bitmap? = null

    fun setBrushBitmap(originalBitmap: Bitmap, strokeWidth: Float, color: Int, alpha: Int) {
        this.originalBitmap = originalBitmap

        scaledBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            strokeWidth.toInt(),
            strokeWidth.toInt(),
            true
        )

        val colorWithAlpha = (alpha shl 24) or (color and 0xFFFFFF)

        scaledBitmap?.let { BitmapUtil.replaceNonTransparentPixels(it, colorWithAlpha) }
    }

    fun updateBrushTextureBitmap(newSize: Int, color: Int, alpha: Int) {
        if (newSize == 0) return
        scaledBitmap = originalBitmap?.let {
            Bitmap.createScaledBitmap(
                it,
                newSize,
                newSize,
                true
            )
        }
        scaledBitmap?.let {
            BitmapUtil.replaceNonTransparentPixels(
                it,
                (alpha shl 24) or (color and 0xFFFFFF))
        }
    }

    fun draw(canvas: Canvas, path: Path, strokeWidth: Float) {
        scaledBitmap?.let { draw(canvas, path, it, strokeWidth) }
    }

    fun draw(canvas: Canvas, path: Path, bitmap: Bitmap, strokeWidth: Float) {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val centerX = bitmapWidth / 2.0f
        val centerY = bitmapHeight / 2.0f

        val pathMeasure = PathMeasure(path, false)
        var distance = 0f

        val position = FloatArray(2)
        val slope = FloatArray(2)

        while (distance < pathMeasure.length) {
            if (pathMeasure.getPosTan(distance, position, slope)) {
                val slopeDegree = (atan2(slope[1], slope[0]) * 180f / PI).toFloat()

                canvas.save()
                canvas.translate(position[0] - centerX, position[1] - centerY)
                canvas.rotate(slopeDegree, centerX, centerY)


                val paintObject = Paint().apply {
                    this.strokeWidth = strokeWidth
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    isAntiAlias = true
                    isFilterBitmap = true
                    isDither = true
                    alpha = 255
                }

                canvas.drawBitmap(bitmap, 0f, 0f, paintObject)
                canvas.restore()

                // Increment the distance by half the bitmap's width to achieve overlap
                distance += bitmapWidth / 1.5f
            }
        }
    }


    fun resetBitmaps() {
        originalBitmap = null
        scaledBitmap = null
    }

}
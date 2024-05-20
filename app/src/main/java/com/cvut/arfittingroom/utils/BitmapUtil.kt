package com.cvut.arfittingroom.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import com.cvut.arfittingroom.model.BITMAP_SIZE

/**
 * Bitmap util
 *
 * @author Veronika Ovsyannikova
 */
object BitmapUtil {
    /**
     * Modifies all non-transparent pixels in the provided bitmap to the specified new color
     * while preserving their original alpha values.
     *
     * @param bitmap The original bitmap to modify.
     * @param newColor The new color to apply, specified as an ARGB integer.
     */
    fun replaceNonTransparentPixels(
        bitmap: Bitmap,
        newColor: Int,
    ) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val newAlpha = Color.alpha(newColor)
        val newRed = Color.red(newColor)
        val newGreen = Color.green(newColor)
        val newBlue = Color.blue(newColor)

        for (i in pixels.indices) {
            val oldColor = pixels[i]
            val oldAlpha = Color.alpha(oldColor)

            val combinedAlpha = (oldAlpha * newAlpha) / 255

            val red = (Color.red(oldColor) * (255 - newAlpha) / 255) + (newRed * newAlpha / 255)
            val green =
                (Color.green(oldColor) * (255 - newAlpha) / 255) + (newGreen * newAlpha / 255)
            val blue = (Color.blue(oldColor) * (255 - newAlpha) / 255) + (newBlue * newAlpha / 255)

            pixels[i] = Color.argb(combinedAlpha, red, green, blue)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    fun combineBitmaps(
        bitmaps: List<Bitmap>,
        width: Int,
        height: Int,
    ): Bitmap {
        val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combinedBitmap)

        bitmaps.forEach {
            val scale = (width.toFloat() / it.width).coerceAtMost(height.toFloat() / it.height)

            val matrix =
                Matrix().apply {
                    postScale(scale, scale)
                    postTranslate((width.toFloat() - it.width * scale) / 2, (height.toFloat() - it.height * scale) / 2)
                }

            canvas.drawBitmap(it, matrix, null)
        }

        return combinedBitmap
    }

    fun adjustBitmapFromEditor(
        bitmap: Bitmap,
        height: Int,
        width: Int,
    ): Bitmap {
        val newY = (height - width) / 2

        val croppedBitmap = Bitmap.createBitmap(bitmap, 0, newY, width, width)

        val matrix =
            Matrix().apply {
                postScale(
                    -1f,
                    1f,
                    croppedBitmap.width / 2f,
                    croppedBitmap.height / 2f,
                )
            }

        val mirroredBitmap =
            Bitmap.createBitmap(
                croppedBitmap,
                0,
                0,
                croppedBitmap.width,
                croppedBitmap.height,
                matrix,
                true,
            )

        return Bitmap.createScaledBitmap(mirroredBitmap, BITMAP_SIZE, BITMAP_SIZE, true)
    }
}

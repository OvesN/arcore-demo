package com.cvut.arfittingroom.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.cvut.arfittingroom.model.TRANSPARENT_CODE

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

        for (i in pixels.indices) {
            val alpha = Color.alpha(pixels[i])
            if (alpha != 0) {
                // Check if the pixel is not completely transparent
                pixels[i] = (alpha shl 24) or (newColor and TRANSPARENT_CODE.toInt())
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
}

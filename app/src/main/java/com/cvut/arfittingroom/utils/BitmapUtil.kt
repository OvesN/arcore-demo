package com.cvut.arfittingroom.utils

import android.graphics.Bitmap
import android.graphics.Color

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

        // Extract the components of the new color
        val newAlpha = Color.alpha(newColor)
        val newRed = Color.red(newColor)
        val newGreen = Color.green(newColor)
        val newBlue = Color.blue(newColor)

        for (i in pixels.indices) {
            val oldColor = pixels[i]
            val oldAlpha = Color.alpha(oldColor)

            // Calculate new alpha based on original alpha and the alpha of the new color
            val combinedAlpha = (oldAlpha * newAlpha) / 255

            // Blend other color components based on new combined alpha
            val red = (Color.red(oldColor) * (255 - newAlpha) / 255) + (newRed * newAlpha / 255)
            val green = (Color.green(oldColor) * (255 - newAlpha) / 255) + (newGreen * newAlpha / 255)
            val blue = (Color.blue(oldColor) * (255 - newAlpha) / 255) + (newBlue * newAlpha / 255)

            // Apply the new pixel color to the array
            pixels[i] = Color.argb(combinedAlpha, red, green, blue)
        }

        // Update the bitmap with the new pixels
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
}

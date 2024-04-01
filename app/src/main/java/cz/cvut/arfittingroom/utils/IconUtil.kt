package cz.cvut.arfittingroom.utils

import android.graphics.Bitmap
import android.graphics.Color

object IconUtil {
    fun changeIconColor(icon: Bitmap, newColor: Int): Bitmap {
        val width = icon.width
        val height = icon.height
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = icon.getPixel(x, y)
                val alpha = Color.alpha(pixel)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Calculate the brightness of the pixel (simple grayscale conversion)
                val brightness = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()

                // If the pixel is a shade of black, replace it with the new color but keep the original alpha
                if (brightness < 128) {  // You can adjust this threshold based on your needs
                    val newPixel = Color.argb(alpha, Color.red(newColor), Color.green(newColor), Color.blue(newColor))
                    outputBitmap.setPixel(x, y, newPixel)
                } else {
                    outputBitmap.setPixel(x, y, pixel)
                }
            }
        }
        return outputBitmap
    }
}
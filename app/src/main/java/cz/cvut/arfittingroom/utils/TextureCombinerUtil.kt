package cz.cvut.arfittingroom.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

object TextureCombinerUtil {

    // Combine two drawables into a single Bitmap
    fun combineDrawables(layers: List<Drawable>): Bitmap? {
        if (layers.isEmpty()) return null

        val bitmap = Bitmap.createBitmap(
            layers.first().intrinsicWidth,
            layers.first().intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        layers.forEach { it.setBounds(0, 0, canvas.width, canvas.height) }
        layers.forEach { it.draw(canvas) }

        return bitmap
    }
}
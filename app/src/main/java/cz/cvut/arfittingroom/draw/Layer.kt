package cz.cvut.arfittingroom.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

class Layer(width: Int, height: Int) {
    var bitmap: Bitmap
    var isVisible: Boolean = true
    var opacity: Float = 1.0f // Range from 0.0 (fully transparent) to 1.0 (fully opaque)

    init {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun draw(canvas: Canvas) {
        if (isVisible) {
            val paint = Paint().apply {
                alpha = (opacity * 255).toInt()
            }
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        }
    }
}
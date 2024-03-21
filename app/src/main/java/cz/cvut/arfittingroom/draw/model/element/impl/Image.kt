package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.command.Resizable
import cz.cvut.arfittingroom.draw.model.element.Element


class Image(
    private val imageBitmap: Bitmap,
    private val resourceId: Int,
    private val matrix: Matrix,
    private val posX: Int,
    private val posY: Int,
    private val scaleFactor: Float,
) : Element(), Drawable{
    override fun draw(canvas: Canvas) {
        matrix.reset()
        matrix.postTranslate(-imageBitmap.width / 2f, -imageBitmap.height / 2f)
        matrix.postScale(scaleFactor, scaleFactor)
        matrix.postTranslate(posX + imageBitmap.width / 2f, posY + imageBitmap.height / 2f)

        canvas.drawBitmap(imageBitmap, matrix, null)
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }
}
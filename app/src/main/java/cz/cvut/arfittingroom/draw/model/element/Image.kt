package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.command.Resizable

class Image(val id: Int):  Movable, Resizable, Drawable {
    private lateinit var bitmap: Bitmap

    private fun drawImage(image: Int) {
//        bitmap = BitmapFactory.decodeResource(resources, image)
//        invalidate()
    }

    override fun move() {
        TODO("Not yet implemented")
    }

    override fun changeSize() {
        TODO("Not yet implemented")
    }

    override fun draw(canvas: Canvas) {
        TODO("Not yet implemented")
    }

}
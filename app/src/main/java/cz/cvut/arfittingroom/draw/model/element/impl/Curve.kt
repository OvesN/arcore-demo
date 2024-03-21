package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath

class Curve(
    private val path: DrawablePath,
    private val paint: Paint
) : Element() {
    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun move() {
        TODO("Not yet implemented")
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun scale(factor: Float) {
        TODO("Not yet implemented")
    }

    override fun endScale() {
        TODO("Not yet implemented")
    }
}
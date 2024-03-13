package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.path.DrawablePath

class Curve(
    private val path: DrawablePath,
    private val paint: Paint
) : Drawable {
    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }
}
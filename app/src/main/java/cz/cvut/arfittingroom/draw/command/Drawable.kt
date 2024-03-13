package cz.cvut.arfittingroom.draw.command

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.PaintOptions

interface Drawable {
    fun draw(canvas: Canvas)
}
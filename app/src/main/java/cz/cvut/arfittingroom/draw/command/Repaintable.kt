package cz.cvut.arfittingroom.draw.command

import android.graphics.Paint

interface Repaintable {
    fun repaint(newColor: Int)

    val paint: Paint
}
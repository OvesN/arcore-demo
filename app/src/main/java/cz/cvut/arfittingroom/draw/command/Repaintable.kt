package cz.cvut.arfittingroom.draw.command

import android.graphics.Color
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.model.PaintOptions

interface Repaintable {
    fun repaint(newColor: Int)

    val paint: Paint
}
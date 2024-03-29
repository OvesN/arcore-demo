package cz.cvut.arfittingroom.draw.command

import android.graphics.Color
import cz.cvut.arfittingroom.draw.model.PaintOptions

interface Repaintable {
    fun repaint(newPaint: PaintOptions)
}
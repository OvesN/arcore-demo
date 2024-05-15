package com.cvut.arfittingroom.draw.command

import android.graphics.Paint
import android.icu.text.ListFormatter.Width

interface Repaintable {
    val paint: Paint

    fun repaint(newColor: Int, fill: Boolean)
}

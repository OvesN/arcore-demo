package com.cvut.arfittingroom.draw.command

import android.graphics.Paint

interface Repaintable {
    val paint: Paint

    fun repaint(newColor: Int)
}

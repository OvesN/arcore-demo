package com.cvut.arfittingroom.draw.command

import android.graphics.Paint

/**
 * Repaintable
 *
 * @author Veronika Ovsyannikova
 */
interface Repaintable {
    val paint: Paint

    fun repaint(newColor: Int, fill: Boolean)
}

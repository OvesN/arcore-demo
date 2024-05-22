package com.cvut.arfittingroom.draw.model.element

import android.graphics.Paint

/**
 * Object that can be repainted
 *
 * @author Veronika Ovsyannikova
 */
interface Repaintable {
    val paint: Paint

    fun repaint(newColor: Int, fill: Boolean)
}

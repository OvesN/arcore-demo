package com.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas

/**
 * Object that can be drawn on a Canvas
 *
 * @author Veronika Ovsyannikova
 */
interface Drawable {
    fun draw(canvas: Canvas)
}

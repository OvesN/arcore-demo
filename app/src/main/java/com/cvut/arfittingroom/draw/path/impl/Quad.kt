package com.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.PathAction

/**
 * Represents an action that draws a quadratic Bezier curve
 *
 * @property x1 The x-coordinate of the control point
 * @property y1 The y-coordinate of the control point
 * @property x2 The x-coordinate of the end point
 * @property y2 The y-coordinate of the end point
 *
 * @author Veronika Ovsyannikova
 */
class Quad(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
) :
    PathAction {
    override fun perform(path: Path) {
        path.quadTo(x1, y1, x2, y2)
    }
}

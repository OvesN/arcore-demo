package com.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.PathAction

/**
 * Represents an action that draws a line to a specified point
 *
 * @property x The x-coordinate of the end point of the line
 * @property y The y-coordinate of the end point of the line
 *
 * @author Veronika Ovsyannikova
 */
class Line(val x: Float, val y: Float) : PathAction {
    override fun perform(path: Path) {
        path.lineTo(x, y)
    }
}

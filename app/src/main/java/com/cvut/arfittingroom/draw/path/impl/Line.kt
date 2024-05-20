package com.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.PathAction

/**
 * Line
 *
 * @property x
 * @property y
 *
 * @author Veronika Ovsyannikova
 */
class Line(val x: Float, val y: Float) : PathAction {
    override fun perform(path: Path) {
        path.lineTo(x, y)
    }
}

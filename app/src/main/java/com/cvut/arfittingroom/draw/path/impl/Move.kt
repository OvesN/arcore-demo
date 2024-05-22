package com.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.PathAction

/**
 * Represents an action that moves to a specified point without drawing
 *
 * @property x The x-coordinate of the point to move to
 * @property y The y-coordinate of the point to move to
 *
 * @author Veronika Ovsyannikova
 */
class Move(val x: Float, val y: Float) : PathAction {
    override fun perform(path: Path) {
        path.moveTo(x, y)
    }
}

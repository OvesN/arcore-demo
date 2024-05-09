package com.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.PathAction
import java.io.Writer

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

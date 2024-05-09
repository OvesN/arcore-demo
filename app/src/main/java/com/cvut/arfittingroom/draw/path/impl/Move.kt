package com.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.PathAction
import java.io.Writer

class Move( val x: Float,  val y: Float) : PathAction {
    override fun perform(path: Path) {
        path.moveTo(x, y)
    }
}

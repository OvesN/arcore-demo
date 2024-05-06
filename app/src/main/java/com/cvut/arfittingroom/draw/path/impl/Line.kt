package com.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.Action
import java.io.Writer

class Line(private val x: Float, private val y: Float) : Action {
    override fun perform(path: Path) {
        path.lineTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("L$x,$y")
    }
}

package cz.cvut.arfittingroom.draw.path.impl

import android.graphics.Path
import cz.cvut.arfittingroom.draw.path.Action
import java.io.Writer

class Move(private val x: Float, private val y: Float) : Action {

    override fun perform(path: Path) {
        path.moveTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("M$x,$y")
    }
}
package cz.cvut.arfittingroom.draw.shape

import android.graphics.Path
import cz.cvut.arfittingroom.draw.Action
import java.io.Writer

class Line(val x: Float, val y: Float) : Action {

    override fun perform(path: Path) {
        path.lineTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("L$x,$y")
    }
}
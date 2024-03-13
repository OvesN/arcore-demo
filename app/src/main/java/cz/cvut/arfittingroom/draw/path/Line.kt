package cz.cvut.arfittingroom.draw.path

import android.graphics.Path
import cz.cvut.arfittingroom.draw.path.Action
import java.io.Writer

class Line(private val x: Float, private val y: Float) : Action {

    override fun perform(path: Path) {
        path.lineTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("L$x,$y")
    }

}
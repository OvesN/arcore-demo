package com.cvut.arfittingroom.draw.path

import android.graphics.Path
import com.cvut.arfittingroom.draw.path.impl.Line
import com.cvut.arfittingroom.draw.path.impl.Move
import com.cvut.arfittingroom.draw.path.impl.Quad
import java.io.Serializable
import java.util.LinkedList

/**
 * Drawable path
 *
 * @author Veronika Ovsyannikova
 */
class DrawablePath : Path(), Serializable {
    val actions = LinkedList<PathAction>()

    fun addActions(actions: List<PathAction>) {
        actions.forEach {
            this.actions.add(it)
            it.perform(this)
        }
    }

    override fun reset() {
        actions.clear()
        super.reset()
    }

    override fun moveTo(
        x: Float,
        y: Float,
    ) {
        actions.add(Move(x, y))
        super.moveTo(x, y)
    }

    override fun lineTo(
        x: Float,
        y: Float,
    ) {
        actions.add(Line(x, y))
        super.lineTo(x, y)
    }

    override fun quadTo(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
    ) {
        actions.add(Quad(x1, y1, x2, y2))
        super.quadTo(x1, y1, x2, y2)
    }
}

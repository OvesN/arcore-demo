package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath


class Heart(
    private val cx: Float,
    private val cy: Float,
    private val outerRadius: Float,
    private val paint: Paint
) : Element(), Drawable {
    override fun draw(canvas: Canvas) {
        canvas.drawPath(createPath(), paint)
    }

    override fun doIntersect(x: Int, y: Int): Boolean {
        TODO("Not yet implemented")
    }

    private fun createPath(): DrawablePath {
        val path = DrawablePath()
        // Starting point
        path.moveTo(outerRadius / 2 + cx, outerRadius / 5 + cy)

        // Upper left path
        path.cubicTo(
            5 * outerRadius / 14 + cx, cy,
            cx, outerRadius / 15 + cy,
            outerRadius / 28 + cx, 2 * outerRadius / 5 + cy
        )

        // Lower left path
        path.cubicTo(
            outerRadius / 14 + cx, 2 * outerRadius / 3 + cy,
            3 * outerRadius / 7 + cx, 5 * outerRadius / 6 + cy,
            outerRadius / 2 + cx, outerRadius + cy
        )

        // Lower right path
        path.cubicTo(
            4 * outerRadius / 7 + cx, 5 * outerRadius / 6 + cy,
            13 * outerRadius / 14 + cx, 2 * outerRadius / 3 + cy,
            27 * outerRadius / 28 + cx, 2 * outerRadius / 5 + cy
        )

        // Upper right path
        path.cubicTo(
            outerRadius + cx, outerRadius / 15 + cy,
            9 * outerRadius / 14 + cx, 0f + cy,
            outerRadius / 2 + cx, outerRadius / 5 + cy
        )

        return path
    }

}
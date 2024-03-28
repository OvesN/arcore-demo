package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath

class Curve(
    private val path: DrawablePath,
    private val paint: Paint
) : Element() {
    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun move(x: Float, y: Float) {
        TODO("Not yet implemented")
    }

    override fun endContinuousMove() {
        TODO("Not yet implemented")
    }

    override fun rotate() {
        TODO("Not yet implemented")
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun createBoundingBox(): DrawablePath {
        TODO("Not yet implemented")
    }

    override fun scale(factor: Float) {
        TODO("Not yet implemented")
    }

    override fun endContinuousScale() {
        TODO("Not yet implemented")
    }

    override fun continuousScale(factor: Float) {
        TODO("Not yet implemented")
    }
}
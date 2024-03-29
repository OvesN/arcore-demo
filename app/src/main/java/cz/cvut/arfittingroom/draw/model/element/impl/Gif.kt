package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element

class Gif(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    override var boundingBox: BoundingBox
) : Element() {
    override fun doIntersect(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun scale(newRadius: Float) {
        TODO("Not yet implemented")
    }

    override fun endContinuousScale() {
        TODO("Not yet implemented")
    }

    override fun continuousScale(factor: Float) {
        TODO("Not yet implemented")
    }

    override fun draw(canvas: Canvas) {
        TODO("Not yet implemented")
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

}
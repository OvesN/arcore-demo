package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath

class Curve(
    private val path: DrawablePath,
    var paint: Paint
) : Element() {

    //TODO resolve
    override var centerX: Float = 0f
    override var centerY: Float = 0f
    override var outerRadius: Float = 0f

    var elementPath: DrawablePath
    override var boundingBox: BoundingBox
    var originalRadius: Float

    var originalCenterX: Float
    var originalCenterY: Float

    init {
        elementPath = createPath()
        boundingBox = createBoundingBox()
        originalRadius = outerRadius
        originalCenterX = centerX
        originalCenterY = centerY
    }

    fun createPath(): DrawablePath = path

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun move(x: Float, y: Float) {
        TODO("Not yet implemented")
    }

    override fun endContinuousMove() {
        TODO("Not yet implemented")
    }

    override fun rotate(newRotationAngle: Float) {
        TODO("Not yet implemented")
    }

    override fun endContinuousRotation() {
        TODO("Not yet implemented")
    }

    override fun rotateContinuously(angleDelta: Float) {
        TODO("Not yet implemented")
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }


    override fun scale(newRadius: Float) {
        TODO("Not yet implemented")
    }

    override fun endContinuousScale() {
        TODO("Not yet implemented")
    }

    override fun scaleContinuously(factor: Float) {
        TODO("Not yet implemented")
    }
}
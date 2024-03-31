package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath

class Curve(
    private val path: DrawablePath,
    private var paint: Paint
) : Element() {

    //TODO resolve
    override var centerX: Float = 0f
    override var centerY: Float = 0f
    override var outerRadius: Float = 0f

    var elementPath: DrawablePath = createPath()
    override var boundingBox: BoundingBox = createBoundingBox()

    override var originalCenterX = centerX
    override var originalCenterY = centerY
    override var originalRadius = outerRadius

    private fun createPath(): DrawablePath = path

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        return super.doIntersect(x, y)
    }
}
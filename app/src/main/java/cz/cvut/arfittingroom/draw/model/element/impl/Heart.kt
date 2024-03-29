package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Figure
import cz.cvut.arfittingroom.draw.path.DrawablePath


class Heart(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    override var paint: Paint
) : Figure() {
    override var elementPath: DrawablePath
    override var boundingBox: BoundingBox
    override var originalRadius: Float
    override var originalCenterX: Float
    override var originalCenterY: Float

    init {
        elementPath = createPath()
        boundingBox = createBoundingBox()
        originalRadius = outerRadius
        originalCenterX = centerX
        originalCenterY = centerY
    }
    
    override fun draw(canvas: Canvas) {
        canvas.drawPath(createPath(), paint)
    }

    override fun changeColor(newColor: Color) {
        TODO("Not yet implemented")
    }

    override fun createPath(): DrawablePath {
        val path = DrawablePath()
        // Starting point
        path.moveTo(outerRadius / 2 + centerX, outerRadius / 5 + centerY)

        // Upper left path
        path.cubicTo(
            5 * outerRadius / 14 + centerX, centerY,
            centerX, outerRadius / 15 + centerY,
            outerRadius / 28 + centerX, 2 * outerRadius / 5 + centerY
        )

        // Lower left path
        path.cubicTo(
            outerRadius / 14 + centerX, 2 * outerRadius / 3 + centerY,
            3 * outerRadius / 7 + centerX, 5 * outerRadius / 6 + centerY,
            outerRadius / 2 + centerX, outerRadius + centerY
        )

        // Lower right path
        path.cubicTo(
            4 * outerRadius / 7 + centerX, 5 * outerRadius / 6 + centerY,
            13 * outerRadius / 14 + centerX, 2 * outerRadius / 3 + centerY,
            27 * outerRadius / 28 + centerX, 2 * outerRadius / 5 + centerY
        )

        // Upper right path
        path.cubicTo(
            outerRadius + centerX, outerRadius / 15 + centerY,
            9 * outerRadius / 14 + centerX, 0f + centerY,
            outerRadius / 2 + centerX, outerRadius / 5 + centerY
        )

        return path
    }

}
package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element

class Gif(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float
) : Element() {
    override var boundingBox: BoundingBox = createBoundingBox()
    override var originalRadius: Float = outerRadius

    override var originalCenterX: Float = centerX
    override var originalCenterY: Float = centerY


    override fun draw(canvas: Canvas) {
        TODO("Not yet implemented")
    }
}
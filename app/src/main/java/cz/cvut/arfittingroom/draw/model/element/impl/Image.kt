package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.element.Figure
import cz.cvut.arfittingroom.draw.path.DrawablePath


//TODO what to do with this??
class Image(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
) : Element(){

    override var boundingBox: BoundingBox
     var originalRadius: Float
     var originalCenterX: Float
     var originalCenterY: Float


    init {
        boundingBox = createBoundingBox()
        originalRadius = outerRadius
        originalCenterX = centerX
        originalCenterY = centerY
    }

    override fun draw(canvas: Canvas) {
//        matrix.reset()
//        matrix.postTranslate(-imageBitmap.width / 2f, -imageBitmap.height / 2f)
//        matrix.postScale(scaleFactor, scaleFactor)
//        matrix.postTranslate(posX + imageBitmap.width / 2f, posY + imageBitmap.height / 2f)
//
//        canvas.drawBitmap(imageBitmap, matrix, null)
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
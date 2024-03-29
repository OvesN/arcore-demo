package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import cz.cvut.arfittingroom.draw.command.ColorChangeable
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import cz.cvut.arfittingroom.draw.path.DrawablePath
import kotlin.math.max

class Figure(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    private val pathCreationStrategy: PathCreationStrategy,
    private var paint: Paint
) : Element(), ColorChangeable {

    override var boundingBox: BoundingBox

    private var elementPath: DrawablePath

    private var originalCenterX: Float
    private var originalCenterY: Float
    private var originalRadius: Float

    init {
        elementPath = createPath()
        boundingBox = createBoundingBox()
        originalRadius = outerRadius
        originalCenterX = centerX
        originalCenterY = centerY
    }

    private fun createPath(): DrawablePath = pathCreationStrategy.createPath(centerX, centerY, outerRadius)

    override fun scale(newRadius: Float) {
        outerRadius = max(newRadius, 1f)

        elementPath = createPath()
        boundingBox = createBoundingBox()
    }

    // Scale while scaling gesture
    override fun scaleContinuously(factor: Float) {
        val newRadius = max(factor * originalRadius, 1f)

        outerRadius = newRadius
        elementPath = createPath()
        boundingBox = createBoundingBox()
    }

    // End of the scale gesture by the user
    // Returns radius to the original one so scale action can be applied correctly
    override fun endContinuousScale() {
        outerRadius = originalRadius
    }

    override fun move(x: Float, y: Float) {
        centerX = x
        centerY = y

        elementPath = createPath()
        boundingBox = createBoundingBox()
    }

    // End of the move gesture by the user
    // Returns center x and y to the original values so move action can be applied correctly
    override fun endContinuousMove() {
        centerX = originalCenterX
        centerY = originalCenterY
    }

    override fun rotate(newRotationAngle: Float) {
        rotationAngle = newRotationAngle
        originalRotationAngle = newRotationAngle
    }

    override fun rotateContinuously(angleDelta: Float) {
        rotationAngle = originalRotationAngle + angleDelta
    }

    override fun endContinuousRotation() {
        rotationAngle = originalRotationAngle
    }

    override fun doIntersect(x: Float, y: Float): Boolean {
        val rectF = RectF()
        boundingBox.elementPath.computeBounds(rectF, true)
        return rectF.contains(x, y)
    }

    override fun draw(canvas: Canvas) {
        canvas.save()

        // Rotate the canvas around the element's center
        canvas.rotate(rotationAngle, centerX, centerY)

        // Draw the element's path
        canvas.drawPath(elementPath, paint)

        // If element is selected, draw  bounding box around it
        if (isSelected) {
            boundingBox.draw(canvas)
        }

        canvas.restore()
    }

    override fun changeColor(newColor: Color) {
        TODO("Not yet implemented")
    }
}
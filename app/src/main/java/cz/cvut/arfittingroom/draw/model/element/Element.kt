package cz.cvut.arfittingroom.draw.model.element

import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.command.Drawable
import cz.cvut.arfittingroom.draw.command.Movable
import cz.cvut.arfittingroom.draw.command.Rotatable
import cz.cvut.arfittingroom.draw.command.Scalable
import java.util.UUID
import kotlin.math.max

abstract class Element : Scalable, Drawable, Movable, Rotatable {
    val id: UUID = UUID.randomUUID()

    abstract var centerX: Float
    abstract var centerY: Float
    abstract var outerRadius: Float
    abstract var boundingBox: BoundingBox

    abstract var originalCenterX: Float
    abstract var originalCenterY: Float
    abstract var originalRadius: Float

    var rotationAngle = 0f
    private var originalRotationAngle = 0f
    private var isSelected: Boolean = false

    override fun draw(canvas: Canvas) {
        drawSpecific(canvas)

        if (isSelected) {
            boundingBox = createBoundingBox()
            boundingBox.draw(canvas)
        }
    }

    abstract fun drawSpecific(canvas: Canvas)

    protected fun createBoundingBox(): BoundingBox =
        BoundingBox(centerX, centerY, outerRadius)

    override fun move(x: Float, y: Float) {
        centerX = x
        centerY = y
    }

    // End of the move gesture by the user
    // Returns center x and y to the original values so move action can be applied correctly
    override fun endContinuousMove() {
        centerX = originalCenterX
        centerY = originalCenterY
    }

    override fun scale(newRadius: Float) {
        outerRadius = max(newRadius, 1f)
        originalRadius = outerRadius
    }

    // Scale while scaling gesture
    override fun scaleContinuously(factor: Float) {
        val newRadius = max(factor * originalRadius, 1f)

        outerRadius = newRadius
    }

    // End of the scale gesture by the user
    // Returns radius to the original one so scale action can be applied correctly
    override fun endContinuousScale() {
        outerRadius = originalRadius
    }

    override fun rotate(newRotationAngle: Float) {
        val normalizedAngle = normalizeAngle(newRotationAngle)
        rotationAngle = normalizedAngle
        originalRotationAngle = normalizedAngle
    }

    override fun rotateContinuously(angleDelta: Float) {
        rotationAngle = normalizeAngle(originalRotationAngle + angleDelta)
    }

    override fun endContinuousRotation() {
        rotationAngle = originalRotationAngle
    }


    open fun doIntersect(x: Float, y: Float): Boolean {
        return boundingBox.contains(x, y)
    }

    open fun setSelected(isSelected: Boolean) {
        this.isSelected = isSelected
    }

    fun isSelected() = isSelected

    private fun normalizeAngle(angle: Float): Float {
        var normalizedAngle = angle % 360  // Reduce the angle to the range (-360, 360)
        if (normalizedAngle < -180) normalizedAngle += 360  // Adjust if the angle is less than -180
        if (normalizedAngle > 180) normalizedAngle -= 360  // Adjust if the angle is more than 180
        return normalizedAngle
    }
}
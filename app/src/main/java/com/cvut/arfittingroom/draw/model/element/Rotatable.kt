package com.cvut.arfittingroom.draw.model.element

/**
 * Object that can be rotated
 *
 * @author Veronika Ovsyannikova
 */
interface Rotatable {
    fun rotate(newRotationAngle: Float)

    fun endContinuousRotation()

    fun rotateContinuously(angleDelta: Float)
}

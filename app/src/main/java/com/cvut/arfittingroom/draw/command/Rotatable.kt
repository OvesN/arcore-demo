package com.cvut.arfittingroom.draw.command

/**
 * Rotatable
 *
 * @author Veronika Ovsyannikova
 */
interface Rotatable {
    fun rotate(newRotationAngle: Float)

    fun endContinuousRotation()

    fun rotateContinuously(angleDelta: Float)
}

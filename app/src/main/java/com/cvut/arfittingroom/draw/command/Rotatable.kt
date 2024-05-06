package com.cvut.arfittingroom.draw.command

interface Rotatable {
    fun rotate(newRotationAngle: Float)

    fun endContinuousRotation()

    fun rotateContinuously(angleDelta: Float)
}

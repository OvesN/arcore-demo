package com.cvut.arfittingroom.draw.command

interface Scalable {
    fun scale(newRadius: Float)

    fun endContinuousScale()

    fun scaleContinuously(factor: Float)
}

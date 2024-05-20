package com.cvut.arfittingroom.draw.command

/**
 * Scalable
 *
 * @author Veronika Ovsyannikova
 */
interface Scalable {
    fun scale(newRadius: Float)

    fun endContinuousScale()

    fun scaleContinuously(factor: Float)
}

package com.cvut.arfittingroom.draw.model.element

/**
 * Object that can be scaled
 *
 * @author Veronika Ovsyannikova
 */
interface Scalable {
    fun scale(newRadius: Float)

    fun endContinuousScale()

    fun scaleContinuously(factor: Float)
}

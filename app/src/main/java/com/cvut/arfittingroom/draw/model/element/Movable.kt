package com.cvut.arfittingroom.draw.model.element

/**
 * Object that can be moved
 *
 * @author Veronika Ovsyannikova
 */
interface Movable {
    fun move(
        x: Float,
        y: Float,
    )

    fun endContinuousMove()
}

package com.cvut.arfittingroom.draw.command

/**
 * Movable
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

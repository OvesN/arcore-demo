package cz.cvut.arfittingroom.draw.command

interface Movable {
    fun move(x: Float, y: Float)

    fun endContinuousMove()
}
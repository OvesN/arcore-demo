package cz.cvut.arfittingroom.draw.command

interface Scalable {
    fun scale(factor: Float)
    fun endContinuousScale()
    fun continuousScale(factor: Float)
}
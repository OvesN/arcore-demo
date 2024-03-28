package cz.cvut.arfittingroom.draw.command

interface Scalable {
    fun scale(newRadius: Float)
    fun endContinuousScale()
    fun continuousScale(factor: Float)
}
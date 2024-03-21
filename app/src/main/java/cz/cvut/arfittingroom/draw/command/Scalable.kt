package cz.cvut.arfittingroom.draw.command

interface Scalable {
    fun scale(factor: Float)
    fun endScale()
}
package cz.cvut.arfittingroom.draw.model.element.strategy

import cz.cvut.arfittingroom.draw.path.DrawablePath

interface PathCreationStrategy {
    val name: String
    fun createPath(centerX: Float, centerY: Float, outerRadius: Float): DrawablePath
}
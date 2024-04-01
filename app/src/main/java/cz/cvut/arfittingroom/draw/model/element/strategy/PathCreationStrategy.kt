package cz.cvut.arfittingroom.draw.model.element.strategy

import cz.cvut.arfittingroom.draw.path.DrawablePath

interface PathCreationStrategy {
    fun createPath(centerX: Float, centerY: Float, outerRadius: Float): DrawablePath
}
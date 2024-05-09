package com.cvut.arfittingroom.draw.model.element.strategy.impl

import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.path.DrawablePath
import javax.inject.Inject

class CirclePathCreationStrategy @Inject constructor(): PathCreationStrategy {
    override val name: String = "circle"
    override fun createPath(centerX: Float, centerY: Float, outerRadius: Float): DrawablePath {
        return DrawablePath()
    }
}

package com.cvut.arfittingroom.draw.model.element.strategy.impl

import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.path.DrawablePath
import javax.inject.Inject

class RectanglePathCreationStrategy
@Inject
constructor() : PathCreationStrategy {
    override val name: String = "rectangle"

    override fun createPath(
        centerX: Float,
        centerY: Float,
        outerRadius: Float,
    ): DrawablePath {
        val path = DrawablePath()

        // Start at the top-left corner
        path.moveTo(centerX - outerRadius, centerY - outerRadius)

        // Draw line to the top-right corner
        path.lineTo(centerX + outerRadius, centerY - outerRadius)

        // Draw line to the bottom-right corner
        path.lineTo(centerX + outerRadius, centerY + outerRadius)

        // Draw line to the bottom-left corner
        path.lineTo(centerX - outerRadius, centerY + outerRadius)

        // Close the path back to the top-left corner
        path.lineTo(centerX - outerRadius, centerY - outerRadius)

        path.close()
        return path
    }
}

package cz.cvut.arfittingroom.draw.model.element.strategy.impl

import cz.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import cz.cvut.arfittingroom.draw.path.DrawablePath

class RectanglePathCreationStrategy: PathCreationStrategy {
    override fun createPath(centerX: Float, centerY: Float, outerRadius: Float): DrawablePath {
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
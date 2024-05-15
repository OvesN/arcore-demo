package com.cvut.arfittingroom.draw.model.element.strategy.impl

import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.path.DrawablePath
import javax.inject.Inject
import kotlin.math.sqrt

class TrianglePathCreationStrategy
@Inject
constructor() : PathCreationStrategy {
    override val name: String = "triangle"
    override fun createPath(centerX: Float, centerY: Float, outerRadius: Float): DrawablePath {
        val path = DrawablePath()

        val height = outerRadius * (sqrt(3.0) / 2.0).toFloat()

        // Vertex coordinates
        val vertex1Y = centerY - outerRadius

        val vertex2X = centerX - height  // Bottom left vertex
        val vertex2Y = centerY + (outerRadius / 2)

        val vertex3X = centerX + height  // Bottom right vertex
        val vertex3Y = centerY + (outerRadius / 2)

        // Move to the first vertex
        path.moveTo(centerX, vertex1Y)

        // Draw lines to the other two vertices
        path.lineTo(vertex2X, vertex2Y)
        path.lineTo(vertex3X, vertex3Y)

        // Close the path to complete the triangle
        path.close()

        return path
    }
}
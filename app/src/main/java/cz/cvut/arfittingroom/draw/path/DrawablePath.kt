package cz.cvut.arfittingroom.draw.path

import android.graphics.Path
import cz.cvut.arfittingroom.draw.path.impl.Line
import cz.cvut.arfittingroom.draw.path.impl.Move
import cz.cvut.arfittingroom.draw.path.impl.Quad
import cz.cvut.arfittingroom.model.Coordinates
import cz.cvut.arfittingroom.model.Vector
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.LinkedList

class DrawablePath : Path(), Serializable {

    private val actions = LinkedList<Action>()

    private val points = mutableListOf<Vector>()

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()

        val copiedActions = actions.map { it }
        copiedActions.forEach {
            it.perform(this)
        }

    }

    override fun reset() {
        actions.clear()
        super.reset()
    }

    override fun moveTo(x: Float, y: Float) {
        addPoint(x, y)

        actions.add(Move(x, y))
        super.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
        actions.add(Line(x, y))

        addPoint(x, y)
        super.lineTo(x, y)
    }

    override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        actions.add(Quad(x1, y1, x2, y2))

        addPoint(x1, y1)
        addPoint(x2, x2)
        super.quadTo(x1, y1, x2, y2)
    }

    fun calculateBuffer(width: Float): FloatArray {
        if (points.isEmpty()) return floatArrayOf()

        // Each point will create a quad consisting of 6 vertices (2 triangles), and each vertex has 3 components (x, y, z)
        val vertices = FloatArray((points.size - 1) * 6 * 3)
        var idx = 0

        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]

            val direction = (p2 - p1).normalize()
            val perpendicular = Vector(-direction.y, direction.x)

            val v1 = p1 + perpendicular * (width / 2)
            val v2 = p1 - perpendicular * (width / 2)
            val v3 = p2 + perpendicular * (width / 2)
            val v4 = p2 - perpendicular * (width / 2)

            // Add the vertices for the first triangle
            addVertex(vertices, idx, v1); idx += 3
            addVertex(vertices, idx, v2); idx += 3
            addVertex(vertices, idx, v3); idx += 3

            // Add the vertices for the second triangle
            addVertex(vertices, idx, v3); idx += 3
            addVertex(vertices, idx, v2); idx += 3
            addVertex(vertices, idx, v4); idx += 3
        }

        return vertices
    }

    private fun addVertex(vertices: FloatArray, index: Int, vector: Vector) {
        vertices[index] = vector.x
        vertices[index + 1] = vector.y
        vertices[index + 2] = 0f // Assuming z-coordinate is 0 for 2D geometry
    }


    private fun addPoint(x: Float, y: Float) {
        points.add(Vector(x, y))
    }
}
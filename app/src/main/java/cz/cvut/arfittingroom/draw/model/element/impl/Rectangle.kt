package cz.cvut.arfittingroom.draw.model.element.impl

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.ar.core.Coordinates2d
import cz.cvut.arfittingroom.draw.model.element.BoundingBox
import cz.cvut.arfittingroom.draw.model.element.Figure
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.model.Coordinates

class Rectangle(
    override var centerX: Float,
    override var centerY: Float,
    override var outerRadius: Float,
    override var paint: Paint
) : Figure() {
    override var elementPath: DrawablePath
    override var boundingBox: BoundingBox
    override var originalRadius: Float
    override var originalCenterX: Float
    override var originalCenterY: Float

    init {
        elementPath = createPath()
        boundingBox = createBoundingBox()
        originalRadius = outerRadius
        originalCenterX = centerX
        originalCenterY = centerY
    }
    
    override fun createPath(): DrawablePath {
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

    override fun changeColor(newColor: Color) {
        TODO("Not yet implemented")
    }
}
package cz.cvut.arfittingroom.utils

import android.graphics.Path
import android.graphics.PathMeasure
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.glcanvas.GLPaint

fun ICanvasGL.BitmapMatrix.inverse(): ICanvasGL.BitmapMatrix {
    val inverseMatrix = ICanvasGL.BitmapMatrix()

//    inverseMatrix.translate(-this.translationX, -this.translationY)
//    inverseMatrix.rotateZ(-this.rotationAngle)
//    inverseMatrix.scale(1 / this.scaleX, 1 / this.scaleY)

    return inverseMatrix
}
fun ICanvasGL.drawPath(path: Path, paint: GLPaint) {
    val pathMeasure = PathMeasure(path, false)
    val pathLength = pathMeasure.length
    val pos = FloatArray(2)
    val tan = FloatArray(2)

    var distance = 0f
    val speed = pathLength / 100  // Adjust this value based on the desired precision.

    // Start from the first point
    if (!pathMeasure.getPosTan(distance, pos, tan)) {
        return  // Path is empty
    }

    var previousX = pos[0]
    var previousY = pos[1]

    while (distance < pathLength) {
        // Move along the path
        distance += speed
        if (!pathMeasure.getPosTan(distance, pos, tan)) {
            break  // Finished the path
        }

        // Draw a line from the previous point to the current point
        this.drawLine(previousX, previousY, pos[0], pos[1], paint)

        // Update the previous point
        previousX = pos[0]
        previousY = pos[1]

        // When the current segment is done, move to the next
        if (distance > pathMeasure.length && !pathMeasure.nextContour()) {
            break  // No more segments
        }
    }
}

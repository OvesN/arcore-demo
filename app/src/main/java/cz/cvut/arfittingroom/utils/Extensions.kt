package cz.cvut.arfittingroom.utils

import android.graphics.Color
import android.graphics.Path
import android.graphics.PathMeasure
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.glcanvas.GLPaint
import com.chillingvan.canvasgl.shapeFilter.BasicDrawShapeFilter
import cz.cvut.arfittingroom.draw.path.DrawablePath

fun ICanvasGL.BitmapMatrix.inverse(): ICanvasGL.BitmapMatrix {
    val inverseMatrix = ICanvasGL.BitmapMatrix()

//    inverseMatrix.translate(-this.translationX, -this.translationY)
//    inverseMatrix.rotateZ(-this.rotationAngle)
//    inverseMatrix.scale(1 / this.scaleX, 1 / this.scaleY)

    return inverseMatrix
}

//TODO AAAAAAAA
fun ICanvasGL.drawPath(path: DrawablePath, paint: GLPaint) {
    val vertices = path.calculateBuffer(paint.lineWidth)

    if (vertices.isEmpty()) return

    this.drawCircle(200f, 200f, 20f, GLPaint().apply { color = Color.BLACK})
    //this.drawLine(0f, 0f, 1000f, 1000f, paint)
    //val arr = floatArrayOf(300f,300f,300f,400f,400f,400f)
    var counter = 0
    val coord = mutableListOf<Float>()
    vertices.forEach {
        counter+1
        if (counter == 6 ) {
            drawLine(coord[0], coord[0], coord[1], coord[1], paint)
            counter = 0
            coord.clear()
        }
        if( it == 0f) {

        }
        else {
            coord.add(it)
        }

    }

    // glCanvas.drawLine()
    this.drawPolyline(vertices, BasicDrawShapeFilter(), paint)
}


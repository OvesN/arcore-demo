package cz.cvut.arfittingroom.utils

import com.chillingvan.canvasgl.ICanvasGL

fun ICanvasGL.BitmapMatrix.inverse(): ICanvasGL.BitmapMatrix {
    val inverseMatrix = ICanvasGL.BitmapMatrix()

//    inverseMatrix.translate(-this.translationX, -this.translationY)
//    inverseMatrix.rotateZ(-this.rotationAngle)
//    inverseMatrix.scale(1 / this.scaleX, 1 / this.scaleY)

    return inverseMatrix
}
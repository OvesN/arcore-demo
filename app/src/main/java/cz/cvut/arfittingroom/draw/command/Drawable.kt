package cz.cvut.arfittingroom.draw.command

import android.graphics.Canvas
import com.chillingvan.canvasgl.ICanvasGL

interface Drawable {
    fun draw(canvas: ICanvasGL)
}
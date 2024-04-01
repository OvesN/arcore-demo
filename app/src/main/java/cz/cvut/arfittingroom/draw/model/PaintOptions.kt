package cz.cvut.arfittingroom.draw.model

import android.graphics.Color
import android.graphics.Paint.Style

data class PaintOptions(var color: Int = Color.BLACK, var strokeWidth: Float = 8f, var style: Style = Style.STROKE)
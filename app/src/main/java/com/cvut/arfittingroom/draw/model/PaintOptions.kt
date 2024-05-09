package com.cvut.arfittingroom.draw.model

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style

data class PaintOptions(
    var color: Int = Color.BLACK,
    var strokeWidth: Float = 8f,
    var alpha: Int = 255,
    var style: Style = Style.STROKE,
    var strokeCap: Paint.Cap = Paint.Cap.ROUND,
)

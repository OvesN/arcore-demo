package com.cvut.arfittingroom.draw.model

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Shader

data class PaintOptions(
    var color: Int = Color.BLACK,
    var strokeWidth: Float = 8f,
    var alpha: Int = 255,
    var style: Style = Style.STROKE,
    var strokeCap: Paint.Cap = Paint.Cap.ROUND,
    var strokeJoint: Paint.Join = Paint.Join.ROUND,
    var strokeTextureRef: String = "",
)

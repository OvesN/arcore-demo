package com.cvut.arfittingroom.draw.model

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style

data class PaintOptions(
    var color: Int = Color.WHITE,
    var strokeWidth: Float = 8f,
    var alpha: Int = 255,
    var style: Style = Style.FILL,
    var strokeCap: Paint.Cap = Paint.Cap.ROUND,
    var strokeJoint: Paint.Join = Paint.Join.ROUND,
    var strokeTextureRef: String = "",
    var blurRadius: Float = 0f,
    var blurType: BlurMaskFilter.Blur = BlurMaskFilter.Blur.NORMAL
)

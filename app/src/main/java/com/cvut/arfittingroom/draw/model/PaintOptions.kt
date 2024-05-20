package com.cvut.arfittingroom.draw.model

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style

/**
 * Paint options
 *
 * @property color
 * @property strokeWidth
 * @property alpha
 * @property style
 * @property strokeCap
 * @property strokeJoint
 * @property strokeTextureRef
 * @property blurRadius
 * @property blurType
 *
 * @author Veronika Ovsyannikova
 */
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

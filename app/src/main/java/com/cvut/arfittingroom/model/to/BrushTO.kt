package com.cvut.arfittingroom.model.to

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import com.google.firebase.firestore.PropertyName

/**
 * Brush t o
 *
 * @property strokeTextureRef
 * @property strokeCap
 * @property strokeJoint
 * @property blurType
 * @property blurRadius
 * @property id
 *
 * @author Veronika Ovsyannikova
 */
data class BrushTO(
    @get:PropertyName("texture_ref") @set:PropertyName("texture_ref") var strokeTextureRef: String = "",
    @get:PropertyName("stroke_cap") @set:PropertyName("stroke_cap") var strokeCap: Paint.Cap = Paint.Cap.ROUND,
    @get:PropertyName("stroke_joint") @set:PropertyName("stroke_joint") var strokeJoint: Paint.Join = Paint.Join.ROUND,
    @get:PropertyName("blur_type") @set:PropertyName("blur_type") var blurType: BlurMaskFilter.Blur = BlurMaskFilter.Blur.NORMAL,
    @get:PropertyName("blur_radius") @set:PropertyName("blur_radius") var blurRadius: Float = 0f,
    var id: String = ""
)
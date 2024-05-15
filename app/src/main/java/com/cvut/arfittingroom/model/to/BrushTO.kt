package com.cvut.arfittingroom.model.to

import android.graphics.Paint
import com.google.firebase.firestore.PropertyName

data class BrushTO(
    @get:PropertyName("image_preview_ref") @set:PropertyName("image_preview_ref") var imagePreviewRef: String = "",
    @get:PropertyName("texture_ref") @set:PropertyName("texture_ref") var strokeTextureRef: String = "",
    @get:PropertyName("stroke_cap") @set:PropertyName("stroke_cap") var strokeCap: Paint.Cap = Paint.Cap.ROUND,
    @get:PropertyName("stroke_joint") @set:PropertyName("stroke_joint") var strokeJoint: Paint.Join = Paint.Join.ROUND,
    @get:PropertyName("style") @set:PropertyName("style") var style: Paint.Style = Paint.Style.STROKE,
    )
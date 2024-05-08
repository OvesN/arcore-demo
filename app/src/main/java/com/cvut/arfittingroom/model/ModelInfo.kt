package com.cvut.arfittingroom.model

import com.google.firebase.firestore.PropertyName

data class ModelInfo(
    @get:PropertyName("slot") @set:PropertyName("slot") var slot: String = "",
    @get:PropertyName("model_ref") @set:PropertyName("model_ref") var modelRef: String = "",
    @get:PropertyName("image_preview_ref") @set:PropertyName("image_preview_ref") var imagePreviewRef: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "",
)

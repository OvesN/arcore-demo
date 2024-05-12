package com.cvut.arfittingroom.model.to

import com.google.firebase.firestore.PropertyName

data class ImageTO(
    @get:PropertyName("ref") @set:PropertyName("ref") var ref: String = "",
    @get:PropertyName("is_animated") @set:PropertyName("is_animated") var isAnimated: Boolean = false,
    @get:PropertyName("uploaded_by") @set:PropertyName("uploaded_by") var uploadedBy: String = "",
)
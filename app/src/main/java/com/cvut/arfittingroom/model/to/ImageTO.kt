package com.cvut.arfittingroom.model.to

import com.google.firebase.firestore.PropertyName

data class ImageTO(
    @get:PropertyName("image_ref") @set:PropertyName("image_ref") var imageRef: String = "",
    @get:PropertyName("is_animated") @set:PropertyName("is_animated") var isAnimated: Boolean = false,
    @get:PropertyName("uploaded_by") @set:PropertyName("uploaded_by") var uploadedBy: String = "",
)
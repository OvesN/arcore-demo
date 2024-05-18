package com.cvut.arfittingroom.model.to

import com.google.firebase.firestore.PropertyName

data class ModelTO(
    @get:PropertyName("slot") @set:PropertyName("slot") var slot: String = "",
    @get:PropertyName("ref") @set:PropertyName("ref") var ref: String = "",
    @get:PropertyName("preview_ref") @set:PropertyName("preview_ref") var previewRef: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "",
)

package com.cvut.arfittingroom.model

import com.google.firebase.firestore.PropertyName



data class MakeupInfo(
    @get:PropertyName("ref") @set:PropertyName("ref") var ref: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "",
    @get:PropertyName("color") @set:PropertyName("color") var color: Int = -1
)
package com.cvut.arfittingroom.model.to

import com.google.firebase.firestore.PropertyName

/**
 * Makeup transfer object
 * Represents makeup options displayed in the Makeup menu
 *
 * @property ref Reference to makeup texture in Firebase Storage
 * @property type Type of makeup
 * @property color Color with which makeup was applied
 *
 * @author Veronika Ovsyannikova
 */
data class MakeupTO(
    @get:PropertyName("ref") @set:PropertyName("ref") var ref: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "",
    @get:PropertyName("color") @set:PropertyName("color") var color: Int = -1,
)

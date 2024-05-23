package com.cvut.arfittingroom.model.to

import com.google.firebase.firestore.PropertyName

/**
 * Model transfer object
 * Represents accessories options displayed in the Accessories menu
 *
 * @property slot Slot to which model will be applied
 * @property ref Reference to the model in Firebase Storage
 * @property previewRef Reference to the model's preview image in Firebase Storage
 * @property type Type of the model
 *
 * @author Veronika Ovsyannikova
 */
data class ModelTO(
    @get:PropertyName("slot") @set:PropertyName("slot") var slot: String = "",
    @get:PropertyName("ref") @set:PropertyName("ref") var ref: String = "",
    @get:PropertyName("preview_ref") @set:PropertyName("preview_ref") var previewRef: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "",
)

package com.cvut.arfittingroom.model.to

import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Image t o
 *
 * @property ref
 * @property isAnimated
 * @property uploadedBy
 * @property createdAt
 *
 * @author Veronika Ovsyannikova
 */
data class ImageTO(
    @get:PropertyName("ref") @set:PropertyName("ref") var ref: String = "",
    @get:PropertyName("is_animated") @set:PropertyName("is_animated") var isAnimated: Boolean = false,
    @get:PropertyName("uploaded_by") @set:PropertyName("uploaded_by") var uploadedBy: String = "",
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Date? = null
)
package com.cvut.arfittingroom.model.to

import com.cvut.arfittingroom.model.to.drawhistory.EditorStateTO
import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Look transfer object
 * Represents a saved look created by a user, including makeup, models, face mask, editor state, and other metadata
 *
 * @property lookId
 * @property author The author or creator of the look
 * @property appliedMakeup List of applied makeup items in the look
 * @property appliedModels List of applied models in the look
 * @property editorState The state of the editor when the look was created
 * @property name The name given to the look
 * @property previewRef Reference to the preview image of the look in Firebase Storage
 * @property isAnimated Indicates whether the look is animated
 * @property isPublic Indicates whether the look is public or private.
 * @property createdAt
 *
 * @author Veronika Ovsyannikova
 */
data class LookTO(
    @get:PropertyName("look_id") @set:PropertyName("look_id") var lookId: String = "",
    @get:PropertyName("author") @set:PropertyName("author") var author: String = "",
    @get:PropertyName("applied_makeup") @set:PropertyName("applied_makeup") var appliedMakeup: List<MakeupTO> = emptyList(),
    @get:PropertyName("applied_models") @set:PropertyName("applied_models") var appliedModels: List<ModelTO> = emptyList(),
    @get:PropertyName("editor_state") @set:PropertyName("editor_state") var editorState: EditorStateTO = EditorStateTO(),
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("preview_ref") @set:PropertyName("preview_ref") var previewRef: String = "",
    @get:PropertyName("is_animated") @set:PropertyName("is_animated") var isAnimated: Boolean = false,
    @get:PropertyName("is_public") @set:PropertyName("is_public") var isPublic: Boolean = false,
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Date?  = null
)

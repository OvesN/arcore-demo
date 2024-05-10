package com.cvut.arfittingroom.model.to

import com.cvut.arfittingroom.model.to.drawhistory.EditorStateTO
import com.google.firebase.firestore.PropertyName

data class LookTO(
    @get:PropertyName("look_id") @set:PropertyName("look_id") var lookId: String = "",
    @get:PropertyName("author") @set:PropertyName("author") var author: String = "",
    @get:PropertyName("applied_makeup") @set:PropertyName("applied_makeup") var appliedMakeup: List<MakeupTO> = emptyList(),
    @get:PropertyName("applied_models") @set:PropertyName("applied_models") var appliedModels: List<ModelTO> = emptyList(),
    @get:PropertyName("editor_state") @set:PropertyName("editor_state") var editorState: EditorStateTO = EditorStateTO(),
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("image_preview_ref") @set:PropertyName("image_preview_ref") var imagePreviewRef: String = "",
    @get:PropertyName("is_animated") @set:PropertyName("is_animated") var isAnimated: Boolean = false,
    @get:PropertyName("is_public") @set:PropertyName("is_public") var isPublic: Boolean = false,
)
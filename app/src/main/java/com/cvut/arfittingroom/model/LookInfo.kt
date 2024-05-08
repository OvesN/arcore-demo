package com.cvut.arfittingroom.model

import com.google.gson.annotations.SerializedName

data class LookInfo(
    @SerializedName("look_id") val lookId: String = "",
    @SerializedName("author") val author: String = "",
    @SerializedName("applied_makeup") val appliedMakeup: List<MakeupInfo> = emptyList(),
    @SerializedName("applied_models") val appliedModels: List<ModelInfo> = emptyList(),
    @SerializedName("history") val history: String = "",
    @SerializedName("name") val name: String,
    @SerializedName("image_preview_ref") val imagePreviewRef: String = "",
    @SerializedName("is_animated") val isAnimated: Boolean = false,
    @SerializedName("is_public") val isPublic: Boolean = false,
)
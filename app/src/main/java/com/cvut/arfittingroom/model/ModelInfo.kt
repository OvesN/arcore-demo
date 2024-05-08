package com.cvut.arfittingroom.model

import com.google.gson.annotations.SerializedName

data class ModelInfo(
    @SerializedName("slot")val slot: String,
    @SerializedName("model_ref")val modelRef: String,
    @SerializedName("image_preview_ref")val imagePreviewRef: String,
    @SerializedName("type")val type: String,
)

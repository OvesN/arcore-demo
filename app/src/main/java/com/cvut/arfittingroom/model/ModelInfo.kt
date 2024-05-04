package com.cvut.arfittingroom.model

import com.cvut.arfittingroom.model.enums.ENodeType

data class ModelInfo(
    val nodeType: ENodeType,
    val modelRef: String,
    val imagePreviewRef: String,
    val type: String,
)

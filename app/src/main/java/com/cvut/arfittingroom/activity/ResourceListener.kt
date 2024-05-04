package com.cvut.arfittingroom.activity

import com.cvut.arfittingroom.model.ModelInfo

interface ResourceListener {
    fun applyImage(
        type: String,
        ref: String,
        color: Int,
    )

    fun applyModel(modelInfo: ModelInfo)

    fun removeImage(type: String)

    fun removeModel(type: String)

    fun applyLook(lookId: String)

    fun removeLook(lookId: String)
}

package com.cvut.arfittingroom.activity

import com.cvut.arfittingroom.model.LookInfo
import com.cvut.arfittingroom.model.ModelInfo

interface ResourceListener {
    fun applyImage(
        type: String,
        ref: String,
        color: Int,
    )

    fun applyModel(modelInfo: ModelInfo)

    fun removeImage(type: String)

    fun removeModel(slot: String)

    fun applyLook(lookInfo: LookInfo)

    fun removeLook(lookId: String)
}

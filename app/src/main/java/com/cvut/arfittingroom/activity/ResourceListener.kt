package com.cvut.arfittingroom.activity

import com.cvut.arfittingroom.model.LookInfo
import com.cvut.arfittingroom.model.MakeupInfo
import com.cvut.arfittingroom.model.ModelInfo

interface ResourceListener {
    fun applyMakeup(
        makeupInfo: MakeupInfo,
    )

    fun applyModel(modelInfo: ModelInfo)

    fun removeMakeup(type: String)

    fun removeModel(slot: String)

    fun applyLook(lookInfo: LookInfo)

    fun removeLook(lookId: String)
}

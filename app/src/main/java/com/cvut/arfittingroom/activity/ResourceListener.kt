package com.cvut.arfittingroom.activity

import com.cvut.arfittingroom.model.to.LookTO
import com.cvut.arfittingroom.model.to.MakeupTO
import com.cvut.arfittingroom.model.to.ModelTO

interface ResourceListener {
    fun applyMakeup(makeupTO: MakeupTO)

    fun applyModel(modelTO: ModelTO)

    fun removeMakeup(type: String)

    fun removeModel(slot: String)

    fun applyLook(lookTO: LookTO)

    fun removeLook(lookId: String)
}

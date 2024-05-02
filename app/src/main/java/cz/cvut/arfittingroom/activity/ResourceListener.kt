package cz.cvut.arfittingroom.activity

import cz.cvut.arfittingroom.model.ModelInfo
import cz.cvut.arfittingroom.model.enums.ENodeType

interface ResourceListener {
    fun applyImage(type: String, ref: String, color: Int)
    fun applyModel(modelInfo: ModelInfo)
    fun removeImage(type: String)
    fun removeModel(type: String)
}
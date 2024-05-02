package cz.cvut.arfittingroom.model

import com.google.ar.sceneform.rendering.ModelRenderable
import cz.cvut.arfittingroom.model.enums.ENodeType

data class ModelInfo(val nodeType: ENodeType, val modelRef: String, val imagePreviewRef: String, val type: String)

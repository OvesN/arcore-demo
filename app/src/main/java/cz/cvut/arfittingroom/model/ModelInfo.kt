package cz.cvut.arfittingroom.model

import com.google.ar.sceneform.rendering.ModelRenderable
import cz.cvut.arfittingroom.model.enums.EModelType

data class ModelInfo(val modelType: EModelType, val model: ModelRenderable, val modelKey: String = "")

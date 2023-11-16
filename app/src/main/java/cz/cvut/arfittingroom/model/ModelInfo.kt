package cz.cvut.arfittingroom.model

import cz.cvut.arfittingroom.model.enums.EModelType

data class ModelInfo(val modelType: EModelType, val uri: String = "")

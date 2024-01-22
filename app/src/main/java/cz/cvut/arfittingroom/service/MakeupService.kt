package cz.cvut.arfittingroom.service

import cz.cvut.arfittingroom.model.MakeUpState
import cz.cvut.arfittingroom.model.ModelInfo

class MakeupService {
    val makeUpState = MakeUpState(mutableSetOf(), null)
    val loadedModels = mutableMapOf<String, ModelInfo>()
}
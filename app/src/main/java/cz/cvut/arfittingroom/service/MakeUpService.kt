package cz.cvut.arfittingroom.service

import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import cz.cvut.arfittingroom.model.MakeUpState
import cz.cvut.arfittingroom.model.ModelInfo
import cz.cvut.arfittingroom.model.enums.EModelType
import javax.inject.Inject

class MakeUpService {
    val makeUpState = MakeUpState(mutableSetOf(), null)
    val loadedModels = mutableMapOf<String, ModelInfo>()
    val appliedModelKeys = mutableSetOf<String>()

}
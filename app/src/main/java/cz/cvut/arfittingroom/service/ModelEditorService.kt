package cz.cvut.arfittingroom.service

import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import cz.cvut.arfittingroom.model.ModelInfo

class ModelEditorService {
    var loadedModels = mutableMapOf<String, ModelInfo>()

 //   fun changeColor(modelKey: String, color: Color, materialIndex: Int): ModelRenderable {
//        val originalModel = loadedModels[modelKey]
//        require(originalModel != null)
//
//        val coloredModel = originalModel.model.makeCopy()
//
//        coloredModel.getMaterial(materialIndex).setFloat3(
//            "baseColor",
//            color
//        )
//
//        loadedModels[modelKey] = ModelInfo(originalModel.nodeType, coloredModel)
//
//        return coloredModel
//    }
}
package cz.cvut.arfittingroom.service

import io.github.sceneview.math.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import cz.cvut.arfittingroom.model.ModelInfo
import io.github.sceneview.material.setColor

class ModelEditorService {
    var loadedModels = mutableMapOf<String, ModelInfo>()

    fun changeColor(modelKey: String, color: Color, materialIndex: Int): ModelRenderable {
        val originalModel = loadedModels[modelKey]
        require(originalModel != null)

        val coloredModel = originalModel.model.makeCopy()

        coloredModel.getMaterial(materialIndex).setColor(
            "baseColor",
            color
        )

        loadedModels[modelKey] = ModelInfo(originalModel.modelType, coloredModel)

        return coloredModel
    }
}
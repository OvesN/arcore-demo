package com.cvut.arfittingroom.service

import android.graphics.Bitmap
import android.graphics.Canvas
import com.cvut.arfittingroom.model.MakeupInfo
import com.cvut.arfittingroom.model.ModelInfo
import com.cvut.arfittingroom.model.enums.ENodeType

class StateService {
    val appliedMakeUpTypes = mutableMapOf<String, MakeupInfo>()
    var textureBitmap: Bitmap? = null
    val makeUpBitmaps = mutableListOf<Bitmap>()
    val loadedModels = mutableMapOf<ENodeType, ModelInfo>()

    fun areMakeupBitmapsPrepared() = makeUpBitmaps.size == appliedMakeUpTypes.size

    fun combineBitmaps(): Bitmap? {
        if (makeUpBitmaps.isEmpty()) {
            return null
        }

        val bitmap =
            Bitmap.createBitmap(
                makeUpBitmaps.first().width,
                makeUpBitmaps.first().height,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)

        makeUpBitmaps.forEach { canvas.drawBitmap(it, 0f, 0f, null) }

        makeUpBitmaps.clear()

        return bitmap
    }

    fun clearAll() {
        loadedModels.clear()
        textureBitmap = null
        appliedMakeUpTypes.clear()
        loadedModels.clear()
    }
}

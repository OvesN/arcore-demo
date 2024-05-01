package cz.cvut.arfittingroom.service

import android.graphics.Bitmap
import android.graphics.Canvas
import cz.cvut.arfittingroom.model.ModelInfo

//TODO keep state in another way
class MakeupService {
    val appliedMakeUpTypes = mutableMapOf<String, String>()
    var textureBitmap: Bitmap? = null
    val makeUpBitmaps = mutableListOf<Bitmap>()
    val loadedModels = mutableMapOf<String, ModelInfo>()

    fun areMakeupBitmapsPrepared() =
       makeUpBitmaps.size == appliedMakeUpTypes.size


    fun combineBitmaps(): Bitmap? {
        if (makeUpBitmaps.isEmpty()) return null

        val bitmap = Bitmap.createBitmap(
            makeUpBitmaps.first().width,
            makeUpBitmaps.first().height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        makeUpBitmaps.forEach { canvas.drawBitmap(it, 0f, 0f, null) }

        makeUpBitmaps.clear()

        return bitmap
    }
}
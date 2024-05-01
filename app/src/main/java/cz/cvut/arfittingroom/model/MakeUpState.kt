package cz.cvut.arfittingroom.model

import android.graphics.Bitmap


class MakeUpState(
    var appliedMakeUpTypes: MutableSet<String>,
    var textureBitmap: Bitmap? = null
)


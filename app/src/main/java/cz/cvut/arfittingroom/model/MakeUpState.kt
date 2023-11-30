package cz.cvut.arfittingroom.model

import android.graphics.Bitmap
import cz.cvut.arfittingroom.model.enums.EMakeupType


class MakeUpState(
    var appliedMakeUpTypes: MutableSet<EMakeupType>,
    var textureBitmap: Bitmap? = null
)


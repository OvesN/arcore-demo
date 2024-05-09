package com.cvut.arfittingroom.draw.path

import android.graphics.Path
import java.io.Serializable

interface PathAction : Serializable {
    fun perform(path: Path)
}

package com.cvut.arfittingroom.draw.path

import android.graphics.Path
import java.io.Serializable
import java.io.Writer

interface PathAction : Serializable {
    fun perform(path: Path)
}

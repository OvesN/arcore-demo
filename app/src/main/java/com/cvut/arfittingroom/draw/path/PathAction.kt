package com.cvut.arfittingroom.draw.path

import android.graphics.Path
import java.io.Serializable

/**
 * Represents an action that can be performed on the path
 *
 * @author Veronika Ovsyannikova
 */
interface PathAction : Serializable {
    fun perform(path: Path)
}

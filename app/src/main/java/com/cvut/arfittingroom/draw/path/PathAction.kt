package com.cvut.arfittingroom.draw.path

import android.graphics.Path
import java.io.Serializable

/**
 * Path action
 *
 * @author Veronika Ovsyannikova
 */
interface PathAction : Serializable {
    fun perform(path: Path)
}

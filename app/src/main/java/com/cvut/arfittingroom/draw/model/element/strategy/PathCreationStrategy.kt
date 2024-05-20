package com.cvut.arfittingroom.draw.model.element.strategy

import com.cvut.arfittingroom.draw.path.DrawablePath

/**
 * Path creation strategy
 *
 * @author Veronika Ovsyannikova
 */
@JvmSuppressWildcards
interface PathCreationStrategy {
    val name: String

    fun createPath(
        centerX: Float,
        centerY: Float,
        outerRadius: Float,
    ): DrawablePath
}

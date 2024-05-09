package com.cvut.arfittingroom.draw.model.element.strategy.impl

import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.path.DrawablePath
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

class StarPathCreationStrategy
@Inject
constructor() : PathCreationStrategy {
    override val name: String = "star"

    override fun createPath(
        centerX: Float,
        centerY: Float,
        outerRadius: Float,
    ): DrawablePath {
        val section = 2.0 * Math.PI / 5
        val path = DrawablePath()
        val innerRadius = outerRadius / 3
        val startAngle = -Math.PI / 2  // Start angle set to -90 degrees

        path.reset()
        path.moveTo(
            (centerX + outerRadius * cos(startAngle)).toFloat(),
            (centerY + outerRadius * sin(startAngle)).toFloat(),
        )
        path.lineTo(
            (centerX + innerRadius * cos(startAngle + section / 2.0)).toFloat(),
            (centerY + innerRadius * sin(startAngle + section / 2.0)).toFloat(),
        )

        for (i in 1 until 5) {
            path.lineTo(
                (centerX + outerRadius * cos(startAngle + section * i)).toFloat(),
                (centerY + outerRadius * sin(startAngle + section * i)).toFloat(),
            )
            path.lineTo(
                (centerX + innerRadius * cos(startAngle + section * i + section / 2.0)).toFloat(),
                (centerY + innerRadius * sin(startAngle + section * i + section / 2.0)).toFloat(),
            )
        }

        path.close()
        return path
    }
}

package cz.cvut.arfittingroom.draw.model.element.strategy.impl

import cz.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import cz.cvut.arfittingroom.draw.path.DrawablePath

class HeartPathCreationStrategy : PathCreationStrategy {
    override fun createPath(centerX: Float, centerY: Float, outerRadius: Float): DrawablePath {

        val path = DrawablePath()
        // Starting point
        path.moveTo(outerRadius / 2 + centerX, outerRadius / 5 + centerY)

        // Upper left path
        path.cubicTo(
            5 * outerRadius / 14 + centerX, centerY,
            centerX, outerRadius / 15 + centerY,
            outerRadius / 28 + centerX, 2 * outerRadius / 5 + centerY
        )

        // Lower left path
        path.cubicTo(
            outerRadius / 14 + centerX, 2 * outerRadius / 3 + centerY,
            3 * outerRadius / 7 + centerX, 5 * outerRadius / 6 + centerY,
            outerRadius / 2 + centerX, outerRadius + centerY
        )

        // Lower right path
        path.cubicTo(
            4 * outerRadius / 7 + centerX, 5 * outerRadius / 6 + centerY,
            13 * outerRadius / 14 + centerX, 2 * outerRadius / 3 + centerY,
            27 * outerRadius / 28 + centerX, 2 * outerRadius / 5 + centerY
        )

        // Upper right path
        path.cubicTo(
            outerRadius + centerX, outerRadius / 15 + centerY,
            9 * outerRadius / 14 + centerX, 0f + centerY,
            outerRadius / 2 + centerX, outerRadius / 5 + centerY
        )

        return path
    }

}
package com.cvut.arfittingroom.service

import android.graphics.Paint
import com.cvut.arfittingroom.draw.Layer
import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.model.element.impl.Curve
import com.cvut.arfittingroom.draw.model.element.impl.Gif
import com.cvut.arfittingroom.draw.model.element.impl.Image
import com.cvut.arfittingroom.draw.model.element.impl.Stamp
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.path.DrawablePath
import com.cvut.arfittingroom.draw.path.PathAction
import com.cvut.arfittingroom.draw.path.impl.Line
import com.cvut.arfittingroom.draw.path.impl.Move
import com.cvut.arfittingroom.draw.path.impl.Quad
import com.cvut.arfittingroom.model.to.EElementType
import com.cvut.arfittingroom.model.to.EPathActionType
import com.cvut.arfittingroom.model.to.drawhistory.ElementTO
import com.cvut.arfittingroom.model.to.drawhistory.LayerTO
import com.cvut.arfittingroom.model.to.drawhistory.PathActionTO
import com.cvut.arfittingroom.model.to.drawhistory.PathTO
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import javax.inject.Inject

/**
 * Mapper
 *
 * @property strategies
 * @author Veronika Ovsyannikova
 */
class Mapper
@Inject
constructor(private val strategies: Map<String, @JvmSuppressWildcards PathCreationStrategy>) {
    private var width: Int = 0
    private var height: Int = 0

    fun setDimensions(
        width: Int,
        height: Int,
    ) {
        this.width = width
        this.height = height
    }

    private fun paintToPaintTO(paint: Paint) =
        PaintOptions(
            color = paint.color,
            strokeWidth = paint.strokeWidth,
            alpha = paint.alpha,
            style = paint.style,
            strokeCap = paint.strokeCap,
            strokeJoint = paint.strokeJoin,
        )

    private fun paintTOtoPaint(paintOptions: PaintOptions) =
        Paint().apply {
            color = paintOptions.color
            strokeWidth = paintOptions.strokeWidth
            alpha = paintOptions.alpha
            style = paintOptions.style
            strokeCap = paintOptions.strokeCap
            strokeJoin = paintOptions.strokeJoint
        }

    private fun pathToPathTO(drawablePath: DrawablePath) = PathTO(drawablePath.actions.map { pathActionToActionTO(it) })

    private fun pathTOPath(pathTO: PathTO): DrawablePath {
        val drawablePath = DrawablePath()
        drawablePath.addActions(pathTO.actions.map { pathActionTOtoAction(it) })

        return drawablePath
    }

    private fun pathActionTOtoAction(pathActionTO: PathActionTO) =
        when (pathActionTO.actionType) {
            EPathActionType.LINE -> Line(x = pathActionTO.x1 * width, y = pathActionTO.y1 * height)
            EPathActionType.MOVE ->
                Move(x = pathActionTO.x1 * width, y = pathActionTO.y1 * height)

            EPathActionType.QUAD,
            ->
                Quad(
                    x1 = pathActionTO.x1 * width,
                    y1 = pathActionTO.y1 * height,
                    x2 = pathActionTO.x2 * width,
                    y2 = pathActionTO.y2 * height,
                )
        }

    private fun pathActionToActionTO(pathAction: PathAction) =
        when (pathAction) {
            is Line ->
                PathActionTO(
                    actionType = EPathActionType.LINE,
                    x1 = pathAction.x / width,
                    y1 = pathAction.y / height,
                )

            is Move ->
                PathActionTO(
                    actionType = EPathActionType.MOVE,
                    x1 = pathAction.x / width,
                    y1 = pathAction.y / height,
                )

            is Quad ->
                PathActionTO(
                    actionType = EPathActionType.QUAD,
                    x1 = pathAction.x1 / width,
                    y1 = pathAction.y1 / height,
                    x2 = pathAction.x2 / width,
                    y2 = pathAction.y2 / height,
                )

            else -> throw IllegalArgumentException("Unsupported element path action type")
        }

    fun elementToElementTO(element: Element): ElementTO {
        val baseTO =
            ElementTO(
                elementType =
                    when (element) {
                        is Curve -> EElementType.CURVE
                        is Image -> EElementType.IMAGE
                        is Gif -> EElementType.GIF
                        is Stamp -> EElementType.STAMP
                        else -> throw IllegalArgumentException("Unsupported element type")
                    },
                id = element.id.toString(),
                centerX = element.centerX / width,
                centerY = element.centerY / height,
                outerRadius = element.outerRadius / width,
                rotationAngle = element.rotationAngle,
            )

        return when (element) {
            is Curve -> baseTO.copy(
                drawablePath = pathToPathTO(element.path),
                paint =
                    paintToPaintTO(element.paint)
                        .apply {
                            strokeTextureRef = element.strokeTextureRef
                            blurRadius = element.blurRadius
                            blurType = element.blurType
                        },
                xdiff = element.xdiff,
                ydiff = element.ydiff,
                radiusDiff = element.radiusDiff,
            )

            is Image ->
                baseTO.copy(
                    resourceRef = element.resourceRef,
                )

            is Gif ->
                baseTO.copy(
                    resourceRef = element.resourceRef,
                )

            is Stamp ->
                baseTO.copy(
                    paint = paintToPaintTO(element.paint),
                    stampName = element.name,
                )

            else -> throw IllegalArgumentException("Unsupported element type")
        }
    }

    fun layerToLayerTO(
        layer: Layer,
        index: Int,
    ) = LayerTO(
        id = layer.id,
        isVisible = layer.isVisible,
        index = index,
        elements = layer.elements.keys.map { it.toString() },
    )

    fun elementTOtoElement(elementTO: ElementTO): Element =
        when (elementTO.elementType) {
            EElementType.STAMP -> {
                val strategy =
                    strategies[elementTO.stampName]
                        ?: throw IllegalArgumentException("Strategy ${elementTO.stampName}for stamp does not exist")
                Stamp(
                    id = UUID.fromString(elementTO.id),
                    centerX = elementTO.centerX * width,
                    centerY = elementTO.centerY * height,
                    outerRadius = elementTO.outerRadius * width,
                    pathCreationStrategy = strategy,
                    paint = paintTOtoPaint(elementTO.paint),
                    rotationAngle = elementTO.rotationAngle,
                )
            }

            EElementType.GIF ->
                Gif(
                    id = UUID.fromString(elementTO.id),
                    resourceRef = elementTO.resourceRef,
                    centerX = elementTO.centerX * width,
                    centerY = elementTO.centerY * height,
                    outerRadius = elementTO.outerRadius * width,
                    rotationAngle = elementTO.rotationAngle,
                )

            EElementType.IMAGE ->
                Image(
                    id = UUID.fromString(elementTO.id),
                    resourceRef = elementTO.resourceRef,
                    centerX = elementTO.centerX * width,
                    centerY = elementTO.centerY * height,
                    outerRadius = elementTO.outerRadius * width,
                    rotationAngle = elementTO.rotationAngle,
                )

            EElementType.CURVE ->
                Curve(
                    id = UUID.fromString(elementTO.id),
                    centerX = elementTO.centerX * width,
                    centerY = elementTO.centerY * height,
                    outerRadius = elementTO.outerRadius * width,
                    path = pathTOPath(elementTO.drawablePath),
                    paint = paintTOtoPaint(elementTO.paint),
                    rotationAngle = elementTO.rotationAngle,
                    strokeTextureRef = elementTO.paint.strokeTextureRef,
                    blurType = elementTO.paint.blurType,
                    blurRadius = elementTO.paint.blurRadius,
                    xdiff = elementTO.xdiff,
                    ydiff = elementTO.ydiff,
                    radiusDiff = elementTO.radiusDiff,
                )
        }

    fun layerTOtoLayer(layerTO: LayerTO) =
        Layer(
            id = layerTO.id,
            width = width,
            height = height,
        )
}

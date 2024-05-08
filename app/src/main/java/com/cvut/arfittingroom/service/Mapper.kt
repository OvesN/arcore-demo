package com.cvut.arfittingroom.service

import android.graphics.Paint
import com.cvut.arfittingroom.draw.Layer
import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.model.element.impl.Curve
import com.cvut.arfittingroom.draw.model.element.impl.Stamp
import com.cvut.arfittingroom.draw.model.element.impl.Gif
import com.cvut.arfittingroom.draw.model.element.impl.Image
import com.cvut.arfittingroom.model.enums.EElementType
import com.cvut.arfittingroom.model.to.drawhistory.ElementTO
import com.cvut.arfittingroom.model.to.drawhistory.LayerTO
import com.google.firebase.storage.FirebaseStorage

class Mapper {
    private val storage = FirebaseStorage.getInstance()

    private fun paintToPaintTO(paint: Paint) =
        PaintOptions(
            color = paint.color,
            strokeWidth = paint.strokeWidth,
            alpha = paint.alpha,
            style = paint.style,
            strokeCap = paint.strokeCap
        )

    //TODO handle exception
    fun elementToElementTO(element: Element): ElementTO {
        val baseTO = ElementTO(
            elementType = when (element) {
                is Curve -> EElementType.CURVE
                is Image -> EElementType.IMAGE
                is Gif -> EElementType.GIF
                is Stamp -> EElementType.STAMP
                else -> throw IllegalArgumentException("Unsupported element type")
            },
            id = element.id.toString(),
            centerX = element.centerX,
            centerY = element.centerY,
            outerRadius = element.outerRadius,
            rotationAngle = element.rotationAngle
        )

        return when (element) {
            is Curve -> baseTO.copy(
                drawablePath = element.path,
                paint = paintToPaintTO(element.paint)
            )

            is Image -> baseTO.copy(
                resourceRef = element.resourceRef
            )

            is Gif -> baseTO.copy(
                resourceRef = element.resourceRef
            )

            is Stamp -> baseTO.copy(
                paint = paintToPaintTO(element.paint),
                stampName = element.name
            )

            else -> baseTO
        }
    }

    fun layerToLayerTO(layer: Layer, index: Int) =
        LayerTO(
            id = layer.id.toString(),
            isVisible = layer.isVisible,
            index = index,
            elements = layer.getAllElementsIds().map { it.toString() }
        )
}
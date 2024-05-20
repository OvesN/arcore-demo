package com.cvut.arfittingroom.model.to.drawhistory

import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.model.to.EElementType

/**
 * Element t o
 *
 * @property id
 * @property resourceRef
 * @property elementType
 * @property centerX
 * @property centerY
 * @property xdiff
 * @property ydiff
 * @property radiusDiff
 * @property outerRadius
 * @property rotationAngle
 * @property drawablePath
 * @property paint
 * @property stampName
 *
 * @author Veronika Ovsyannikova
 */
data class ElementTO(
    var id: String = "",
    var resourceRef: String = "",
    var elementType: EElementType = EElementType.CURVE,
    var centerX: Float = 0f,
    var centerY: Float = 0f,
    var xdiff: Float = 0f,
    var ydiff: Float = 0f,
    var radiusDiff: Float = 0f,
    var outerRadius: Float = 0f,
    var rotationAngle: Float = 0f,
    var drawablePath: PathTO = PathTO(),
    var paint: PaintOptions = PaintOptions(),
    var stampName: String = "",
)

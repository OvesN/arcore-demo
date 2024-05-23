package com.cvut.arfittingroom.model.to.drawhistory

import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.model.to.EElementType

/**
 * Element transfer object
 * Is used to serialize editor state
 * Holds all possible properties of the Element, type is defined by [elementType]
 *
 * @property id
 * @property resourceRef Reference to the Firebase Storage (Used for GIFs and images)
 * @property elementType
 * @property centerX
 * @property centerY
 * @property xdiff Difference from original x
 * @property ydiff Difference from original y
 * @property radiusDiff Difference from original radius
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

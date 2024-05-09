package com.cvut.arfittingroom.model.to.drawhistory

import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.model.enums.EElementType

data class ElementTO(
    var id: String = "",
    var resourceRef: String = "",
    var elementType: EElementType = EElementType.CURVE,
    var centerX: Float = 0f,
    var centerY: Float = 0f,
    var outerRadius: Float = 0f,
    var rotationAngle: Float = 0f,
    var drawablePath: PathTO = PathTO(),
    var paint: PaintOptions = PaintOptions(),
    var stampName: String = "",
)

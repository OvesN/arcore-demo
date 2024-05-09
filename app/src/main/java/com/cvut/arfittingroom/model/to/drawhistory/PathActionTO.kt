package com.cvut.arfittingroom.model.to.drawhistory

import com.cvut.arfittingroom.model.enums.EPathActionType

data class PathActionTO(
    var actionType: EPathActionType = EPathActionType.LINE,
    var x1: Float = 0f,
    var y1: Float = 0f,
    var x2: Float = 0f,
     var y2: Float = 0f,
)
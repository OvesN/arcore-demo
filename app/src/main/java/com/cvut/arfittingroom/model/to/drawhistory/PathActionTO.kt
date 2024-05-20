package com.cvut.arfittingroom.model.to.drawhistory

import com.cvut.arfittingroom.model.to.EPathActionType

/**
 * Path action t o
 *
 * @property actionType
 * @property x1
 * @property y1
 * @property x2
 * @property y2
 *
 * @author Veronika Ovsyannikova
 */
data class PathActionTO(
    var actionType: EPathActionType = EPathActionType.LINE,
    var x1: Float = 0f,
    var y1: Float = 0f,
    var x2: Float = 0f,
    var y2: Float = 0f,
)

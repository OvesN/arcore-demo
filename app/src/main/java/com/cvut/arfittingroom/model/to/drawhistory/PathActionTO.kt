package com.cvut.arfittingroom.model.to.drawhistory

import com.cvut.arfittingroom.model.to.EPathActionType

/**
 * Path action transfer object
 * Represents a single action in a serialized path
 *Holds all possible properties of the PathAction, type is defined by [actionType]
 *
 * @property actionType
 * @property x1
 * @property y1
 * @property x2 used for Quad
 * @property y2 used for Quad
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

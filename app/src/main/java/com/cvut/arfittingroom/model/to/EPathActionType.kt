package com.cvut.arfittingroom.model.to

/**
 * Helps to distinguish between different child classes of PathActionTO during deserialization
 *
 * @author Veronika Ovsyannikova
 */
enum class EPathActionType {
    LINE,
    MOVE,
    QUAD,
    ;
}

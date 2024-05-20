package com.cvut.arfittingroom.draw.model.enums

/**
 * E element edit action
 *
 * @property actionName
 *
 * @author Veronika Ovsyannikova
 */
enum class EElementEditAction(val actionName: String = "") {
    CHANGE_COLOR("Change color"),
    DELETE,
    MENU,
    LAYER_DOWN("Layer down"),
    TO_LAYER("To Layer"),
    LAYER_UP("Layer up"),
    ROTATE,
    SCALE,
    ;
}

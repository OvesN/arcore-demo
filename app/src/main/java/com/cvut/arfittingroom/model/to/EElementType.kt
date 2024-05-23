package com.cvut.arfittingroom.model.to

/**
 * Helps to distinguish between different child classes of ElementTO during deserialization
 *
 * @author Veronika Ovsyannikova
 */
enum class EElementType {
    CURVE,
    GIF,
    IMAGE,
    STAMP,
    ;
}

package com.cvut.arfittingroom.model.to.drawhistory

/**
 * Layer t o
 *
 * @property id
 * @property index
 * @property elements
 * @property isVisible
 *
 * @author Veronika Ovsyannikova
 */
data class LayerTO(
    var id: Int = 0,
    var index: Int = 0,
    var elements: List<String> = emptyList(),
    var isVisible: Boolean = false,
)

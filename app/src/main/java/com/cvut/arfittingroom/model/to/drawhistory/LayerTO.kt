package com.cvut.arfittingroom.model.to.drawhistory

data class LayerTO(
    var id: Int = 0,
    var index: Int = 0,
    var elements: List<String> = emptyList(),
    var isVisible: Boolean = false,
)

package com.cvut.arfittingroom.model.to.drawhistory

data class DrawHistoryTO(
    var elements: List<ElementTO> = emptyList(),
    var layers: List<LayerTO> = emptyList(),
)

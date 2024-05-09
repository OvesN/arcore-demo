package com.cvut.arfittingroom.model.to.drawhistory

data class EditorStateTO(
    var elements: List<ElementTO> = emptyList(),
    var layers: List<LayerTO> = emptyList(),
)

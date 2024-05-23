package com.cvut.arfittingroom.model.to.drawhistory

/**
 * Editor state transfer objects
 *
 * @property elements
 * @property layers
 *
 * @author Veronika Ovsyannikova
 */
data class EditorStateTO(
    var elements: List<ElementTO> = emptyList(),
    var layers: List<LayerTO> = emptyList(),
)

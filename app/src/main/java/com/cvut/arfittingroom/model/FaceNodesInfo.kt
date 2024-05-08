package com.cvut.arfittingroom.model

import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.ux.AugmentedFaceNode

data class FaceNodesInfo(
    var augmentedFace: AugmentedFace? = null,
    val slotToFaceNodeMap: MutableMap<String, AugmentedFaceNode>,
)

package com.cvut.arfittingroom.model

import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.ux.AugmentedFaceNode

/**
 * Holds information about applied effects to the user's face
 *
 * @property augmentedFace The current AugmentedFace being tracked
 * @property slotToFaceNodeMap
 *
 * @author Veronika Ovsyannikova
 */
data class FaceNodesInfo(
    var augmentedFace: AugmentedFace? = null,
    val slotToFaceNodeMap: MutableMap<String, AugmentedFaceNode>,
)

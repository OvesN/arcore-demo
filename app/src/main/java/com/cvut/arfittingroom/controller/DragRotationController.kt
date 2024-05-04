package com.cvut.arfittingroom.controller

import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.BaseTransformationController
import com.google.ar.sceneform.ux.DragGesture
import com.google.ar.sceneform.ux.DragGestureRecognizer
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

class DragRotationController(
    transformableNode: BaseTransformableNode,
    gestureRecognizer: DragGestureRecognizer,
) :
    BaseTransformationController<DragGesture>(transformableNode, gestureRecognizer) {
    // Rate that the node rotates in degrees per degree of twisting.
    private var rotationRateDegrees = 0.2f

    public override fun canStartTransformation(gesture: DragGesture): Boolean = transformableNode.isSelected

    public override fun onContinueTransformation(gesture: DragGesture) {
        val rotationAmountX = gesture.delta.x * rotationRateDegrees
        // val rotationAmountZ = gesture.delta.y * rotationRateDegrees

        val rotationDeltaX = Quaternion(Vector3.up(), rotationAmountX)
        // val rotationDeltaZ = Quaternion(Vector3.forward(), rotationAmountZ)

        transformableNode.localRotation = Quaternion.multiply(transformableNode.localRotation, rotationDeltaX)
        // transformableNode.localRotation = Quaternion.multiply(Quaternion.multiply(transformableNode.localRotation, rotationDeltaX), rotationDeltaZ)
    }

    public override fun onEndTransformation(gesture: DragGesture) {}
}

class DragTransformableNode(transformationSystem: TransformationSystem) :
    TransformableNode(transformationSystem) {
    private val dragRotationController =
        DragRotationController(
            this,
            transformationSystem.dragRecognizer,
        )

    init {
        translationController.isEnabled = false
        removeTransformationController(translationController)
        removeTransformationController(rotationController)
        addTransformationController(dragRotationController)
    }
}

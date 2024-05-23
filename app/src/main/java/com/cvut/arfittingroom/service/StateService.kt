package com.cvut.arfittingroom.service

import android.graphics.Bitmap
import android.util.Log
import com.cvut.arfittingroom.model.BITMAP_SIZE
import com.cvut.arfittingroom.model.FaceNodesInfo
import com.cvut.arfittingroom.model.MAKEUP_SLOT
import com.cvut.arfittingroom.model.to.MakeupTO
import com.cvut.arfittingroom.model.to.ModelTO
import com.cvut.arfittingroom.utils.BitmapUtil.combineBitmaps
import com.cvut.arfittingroom.utils.BitmapUtil.replaceNonTransparentPixels
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.AugmentedFaceNode

/**
 * State service manages that models and textures should be displayed
 *
 * @author Veronika Ovsyannikova
 */
class StateService {
    val appliedMakeUpTypes = mutableMapOf<String, MakeupTO>()
    var makeupTextureBitmap: Bitmap? = null
    private val makeUpBitmaps = mutableListOf<Bitmap>()
    private val appliedModels = mutableMapOf<String, ModelTO>()
    val faceNodesInfo = FaceNodesInfo(null, mutableMapOf())

    init {
        faceNodesInfo.slotToFaceNodeMap[MAKEUP_SLOT] = AugmentedFaceNode()
    }

    private fun areMakeupBitmapsPrepared() = makeUpBitmaps.size == appliedMakeUpTypes.size

    private fun combineMakeUpBitmaps(): Bitmap? {
        if (makeUpBitmaps.isEmpty()) {
            return null
        }

        val bitmap = combineBitmaps(makeUpBitmaps, BITMAP_SIZE, BITMAP_SIZE)
        makeUpBitmaps.clear()

        return bitmap
    }


    /**
     * Delete all effects applied to the face
     */
    fun clearAll() {
        appliedModels.clear()
        makeupTextureBitmap = null
        appliedMakeUpTypes.clear()
        appliedModels.clear()
        makeUpBitmaps.clear()
        clearFaceNodes()
    }

    /**
     * Clears the face node for the specified slot.
     *
     * @param slot The slot to clear.
     */
    fun clearFaceNodeSlot(slot: String) {
        appliedModels.remove(slot)

        faceNodesInfo.slotToFaceNodeMap[slot]?.let {
            it.parent = null
            faceNodesInfo.slotToFaceNodeMap.remove(slot)
        }
    }

    /**
     * Adds a model to the applied models list
     *
     * @param modelTO The model to add
     */
    fun addModel(modelTO: ModelTO) {
        appliedModels[modelTO.slot] = modelTO
    }


    /**
     * Applies the specified model to the face in the AR scene view
     *
     * @param sceneView The AR scene view
     * @param renderable The model renderable to apply
     * @param slot The slot to apply the model to
     */
    fun applyModelOnFace(
        sceneView: ArSceneView,
        renderable: ModelRenderable,
        slot: String,
    ) {
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            if (faceNodesInfo.augmentedFace != face) {
                reapplyNodesForNewFace(face, sceneView)
            }

            val slotFaceNode = faceNodesInfo.slotToFaceNodeMap[slot]
            if (slotFaceNode != null) {
                slotFaceNode.renderable = renderable
            } else {
                faceNodesInfo.slotToFaceNodeMap[slot] = AugmentedFaceNode(face).also { newNode ->
                    newNode.parent = sceneView.scene
                    newNode.renderable = renderable
                }
            }
        }
    }

    private fun clearFaceNodes() {
        faceNodesInfo.slotToFaceNodeMap.values.forEach {
            it.parent = null
        }
        faceNodesInfo.slotToFaceNodeMap.clear()
    }

    /**
     * Hides face effects if the face tracking state is stopped or paused
     */
    fun hideNodesIfFaceTrackingStopped() {
        faceNodesInfo.augmentedFace?.let { augmentedFace ->
            if (augmentedFace.trackingState == TrackingState.STOPPED || augmentedFace.trackingState == TrackingState.PAUSED) {
                faceNodesInfo.slotToFaceNodeMap.values.forEach { node ->
                    if (node.isEnabled) {
                        node.isEnabled = false
                    }
                }
            } else {
                faceNodesInfo.slotToFaceNodeMap.values.forEach { node ->
                    if (!node.isEnabled) {
                        node.isEnabled = true
                    }
                }
            }
        }
    }

    /**
     * Applies the specified texture to the face node in the given slot
     *
     * @param texture The texture to apply
     * @param sceneView The AR scene view
     * @param slot The slot to apply the texture to
     */
    fun applyTextureToFaceNode(
        texture: Texture,
        sceneView: ArSceneView,
        slot: String,
    ) {
        val faceNode =
            faceNodesInfo.slotToFaceNodeMap.getOrPut(slot) {
                AugmentedFaceNode().apply {
                    parent = sceneView.scene
                    augmentedFace = faceNodesInfo.augmentedFace
                }
            }.apply {
                faceMeshTexture = texture
            }
    }

    /**
     * Creates a texture from the given bitmap and applies it to the face node in the specified slot
     *
     * @param bitmap The bitmap to create the texture from
     * @param sceneView The AR scene view
     * @param slot The slot to apply the texture to
     */
    fun createTextureAndApply(
        bitmap: Bitmap,
        sceneView: ArSceneView,
        slot: String,
    ) {
        // Convert Bitmap to ARCore Texture
        Texture.builder()
            .setSource(bitmap)
            .build()
            .thenAccept { texture -> applyTextureToFaceNode(texture, sceneView, slot) }
            .exceptionally {
                Log.println(Log.ERROR, null, "Error during texture initialisation")
                null
            }
    }

    /**
     * Loads an image, recolors it, and adds it to the makeup bitmaps
     *
     * @param image The image to load
     * @param color The color to apply to the image
     * @param sceneView The AR scene view
     */
    fun loadMakeup(
        image: Bitmap,
        color: Int,
        sceneView: ArSceneView,
    ) {
        replaceNonTransparentPixels(image, color)
        makeUpBitmaps.add(image)

        if (areMakeupBitmapsPrepared()) {
            makeupTextureBitmap = combineMakeUpBitmaps()
            makeupTextureBitmap?.let { createTextureAndApply(it, sceneView, MAKEUP_SLOT) }
        }
    }

    /**
     * Reapplies nodes for a new face detected in the AR scene view
     * Is used because when AR session is paused and resumed the user's face
     * will be treated as a new one
     *
     * @param face The new face to apply nodes to
     * @param sceneView The AR scene view
     */
    fun reapplyNodesForNewFace(
        face: AugmentedFace,
        sceneView: ArSceneView,
    ) {
        faceNodesInfo.augmentedFace = face

        faceNodesInfo.slotToFaceNodeMap.values.forEach {
            it.parent = sceneView.scene
            it.augmentedFace = face
        }
    }

    /**
     * Retrieves the list of applied models
     *
     * @return The list of applied models
     */
    fun getAppliedModelsList() = appliedModels.values.toList()

    /**
     * Retrieves the list of applied makeup types
     *
     * @return The list of applied makeup types
     */
    fun getAppliedMakeupList() = appliedMakeUpTypes.values.toList()
}

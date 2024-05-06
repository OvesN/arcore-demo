package com.cvut.arfittingroom.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import com.cvut.arfittingroom.model.FaceNodesInfo
import com.cvut.arfittingroom.model.MAKEUP_SLOT
import com.cvut.arfittingroom.model.MakeupInfo
import com.cvut.arfittingroom.model.ModelInfo
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
 */
class StateService {
    val appliedMakeUpTypes = mutableMapOf<String, MakeupInfo>()
    var makeupTextureBitmap: Bitmap? = null
    private val makeUpBitmaps = mutableListOf<Bitmap>()
    private val loadedModels = mutableMapOf<String, ModelInfo>()
    val faceNodesInfo = FaceNodesInfo(null, mutableMapOf())

    private fun areMakeupBitmapsPrepared() = makeUpBitmaps.size == appliedMakeUpTypes.size

    private fun combineMakeUpBitmaps(): Bitmap? {
        if (makeUpBitmaps.isEmpty()) {
            return null
        }
        val bitmap =
            Bitmap.createBitmap(
                makeUpBitmaps.first().width,
                makeUpBitmaps.first().height,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)

        makeUpBitmaps.forEach { canvas.drawBitmap(it, 0f, 0f, null) }

        makeUpBitmaps.clear()

        return bitmap
    }

    fun clearAll() {
        loadedModels.clear()
        clearFaceNodes()
        makeupTextureBitmap = null
        appliedMakeUpTypes.clear()
        loadedModels.clear()
    }

    fun clearFaceNodeSlot(slot: String) {
        loadedModels.remove(slot)

        faceNodesInfo.slotToFaceNodeMap[slot]?.let {
            it.parent = null
            faceNodesInfo.slotToFaceNodeMap.remove(slot)
        }

    }

    fun addModel(modelInfo: ModelInfo) {
        loadedModels[modelInfo.slot] = modelInfo
    }

    fun applyModelOnFace(
        sceneView: ArSceneView,
        renderable: ModelRenderable,
        slot: String,
    ) {
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            if (faceNodesInfo.augmentedFace != face) {
                reapplyNodesForNewFace(face, sceneView)
            }

            faceNodesInfo.slotToFaceNodeMap[slot] ?: AugmentedFaceNode(face).also { newNode ->
                newNode.parent = sceneView.scene
                newNode.renderable = renderable
                faceNodesInfo.slotToFaceNodeMap[slot] = newNode
            }.apply {
                this.renderable = renderable
            }
        }
    }

    private fun clearFaceNodes() {
        faceNodesInfo.slotToFaceNodeMap.values.forEach {
            it.parent = null
        }

        faceNodesInfo.slotToFaceNodeMap.clear()
    }

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

    fun applyTextureToFaceNode(
        texture: Texture,
        sceneView: ArSceneView,
        slot: String
    ) {
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            if (faceNodesInfo.augmentedFace != face) {
                reapplyNodesForNewFace(face,sceneView)
            }

            val faceNode =
                faceNodesInfo.slotToFaceNodeMap.getOrPut(slot) {
                    AugmentedFaceNode(face).apply {
                        parent = sceneView.scene
                    }
                }.apply {
                    faceMeshTexture = texture
                }
        }
    }

    fun createTextureAndApply(
        combinedBitmap: Bitmap,
        sceneView: ArSceneView,
        slot: String
    ) {
        // Convert Bitmap to ARCore Texture
        Texture.builder()
            .setSource(combinedBitmap)
            .build()
            .thenAccept { texture -> applyTextureToFaceNode(texture, sceneView, slot) }
            .exceptionally {
                Log.println(Log.ERROR, null, "Error during texture initialisation")
                null
            }
    }

    fun loadImage(
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

    fun reapplyNodesForNewFace(face: AugmentedFace, sceneView: ArSceneView) {
        faceNodesInfo.augmentedFace = face

        faceNodesInfo.slotToFaceNodeMap.values.forEach {
            it.parent = sceneView.scene
            it.augmentedFace = face
        }
    }
}

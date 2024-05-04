package com.cvut.arfittingroom.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import com.cvut.arfittingroom.model.MAKEUP_SLOT
import com.cvut.arfittingroom.model.MakeupInfo
import com.cvut.arfittingroom.model.ModelInfo
import com.cvut.arfittingroom.utils.BitmapUtil
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
    private var textureBitmap: Bitmap? = null
    private val makeUpBitmaps = mutableListOf<Bitmap>()
    val loadedModels = mutableMapOf<String, ModelInfo>()


    private val faceNodeMap = HashMap<AugmentedFace, HashMap<String, AugmentedFaceNode>>()

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
        textureBitmap = null
        appliedMakeUpTypes.clear()
        loadedModels.clear()
    }

    fun clearFaceNodeSlot(slot: String) {
        loadedModels.remove(slot)

        val iterator = faceNodeMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value[slot]?.let {
                it.parent = null
                iterator.remove()
            }
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
            val nodesMap = faceNodeMap.getOrPut(face) { HashMap() }

            val faceNode = nodesMap[slot] ?: AugmentedFaceNode(face).also { newNode ->
                newNode.parent = sceneView.scene
                newNode.renderable = renderable
                nodesMap[slot] = newNode
            }

            faceNode.renderable = renderable
        }

    }


    private fun clearFaceNodes() {
        faceNodeMap.values.forEach { map ->
            map.values.forEach { it.parent = null }
        }

        faceNodeMap.clear()
    }

    fun removeNodesIfFaceTrackingStopped() {
        faceNodeMap.entries.removeIf { (face, nodes) ->
            if (face.trackingState == TrackingState.STOPPED) {
                nodes.forEach { entry ->
                    entry.value.parent = null

                }
                true
            } else {
                false
            }
        }
    }

    fun applyTextureToFaceNode(texture: Texture, sceneView: ArSceneView) {
        sceneView.session?.getAllTrackables(AugmentedFace::class.java)?.forEach { face ->
            val modelNodesMap = faceNodeMap.getOrPut(face) { HashMap() }

            val faceNode = modelNodesMap.getOrPut(MAKEUP_SLOT) {
                AugmentedFaceNode(face).apply {
                    parent = sceneView.scene
                }
            }

            faceNode.faceMeshTexture = texture
        }
    }

    fun createTextureAndApply(combinedBitmap: Bitmap, sceneView: ArSceneView) {
        textureBitmap = combinedBitmap

        // Convert Bitmap to ARCore Texture
        Texture.builder()
            .setSource(combinedBitmap)
            .build()
            .thenAccept { texture -> applyTextureToFaceNode(texture, sceneView) }
            .exceptionally {
                Log.println(Log.ERROR, null, "Error during texture initialisation")
                null
            }
    }

    fun loadImage(image: Bitmap, color: Int, sceneView: ArSceneView) {
        replaceNonTransparentPixels(image, color)
        makeUpBitmaps.add(image)

        if (areMakeupBitmapsPrepared()) {
            val combinedBitmap = combineMakeUpBitmaps()
            combinedBitmap?.let { createTextureAndApply(it, sceneView) }
        }
    }

}

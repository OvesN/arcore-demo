package com.cvut.arfittingroom.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.cvut.arfittingroom.draw.service.LayerManager
import com.cvut.arfittingroom.model.MASK_FRAMES_DIR_NAME
import com.cvut.arfittingroom.model.MASK_FRAME_FILE_NAME
import com.cvut.arfittingroom.model.MASK_TEXTURE_FILE_NAME
import com.cvut.arfittingroom.utils.BitmapUtil.adjustBitmapFromEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

/**
 * File util
 *
 * @author Veronika Ovsyannikova
 */
object FileUtil {
    fun saveTempMaskTextureBitmap(
        bitmap: Bitmap,
        context: Context,
        onSaved: () -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val internalStorageDir = context.filesDir
                val file = File(internalStorageDir, MASK_TEXTURE_FILE_NAME)

                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                withContext(Dispatchers.Main) {
                    onSaved()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTempMaskTextureBitmap(context: Context): Bitmap? =
        try {
            val fis: FileInputStream = context.openFileInput(MASK_TEXTURE_FILE_NAME)

            val bitmap: Bitmap = BitmapFactory.decodeStream(fis)

            fis.close()

            bitmap
        } catch (e: Exception) {
            null
        }

    fun getTempMaskTextureStream(context: Context): FileInputStream? =
        try {
            context.openFileInput(MASK_TEXTURE_FILE_NAME)
        } catch (e: Exception) {
            null
        }

    fun doesTempAnimatedMaskExist(context: Context): Boolean {
        val imagesDir = File(context.filesDir, MASK_FRAMES_DIR_NAME)

        return imagesDir.exists() && imagesDir.isDirectory && imagesDir.list()?.isNotEmpty() == true
    }

    fun getNumberOfFrames(context: Context): Int {
        val imagesDir = File(context.filesDir, MASK_FRAMES_DIR_NAME)

        return imagesDir.list()?.size ?: 0
    }

    fun getNextTempMaskFrame(
        context: Context,
        counter: Int,
    ): Bitmap? =
        try {
            val file =
                File(
                    context.filesDir,
                    "$MASK_FRAMES_DIR_NAME/${MASK_FRAME_FILE_NAME}_$counter.png",
                )

            val fis = FileInputStream(file)

            val bitmap: Bitmap = BitmapFactory.decodeStream(fis)

            fis.close()

            bitmap
        } catch (e: Exception) {
            null
        }

    fun getNextTempMaskFrameInputStream(
        context: Context,
        counter: Int,
    ): FileInputStream? = try {
        val directory = File(context.filesDir, MASK_FRAMES_DIR_NAME)
        val fileName = "${MASK_FRAME_FILE_NAME}_$counter.png"
        val file = File(directory, fileName)

        FileInputStream(file)
    } catch (e: Exception) {
        null
    }

    fun saveTempMaskFrames(
        layerManager: LayerManager,
        height: Int,
        width: Int,
        context: Context,
        onSaved: () -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagesDir = File(context.filesDir, MASK_FRAMES_DIR_NAME)
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val maxNumberOfFrames = layerManager.getMaxNumberOfFrames()
                for (counter in 0 until maxNumberOfFrames) {
                    val fileName = "${MASK_FRAME_FILE_NAME}_$counter.png"
                    val file = File(imagesDir, fileName)
                    FileOutputStream(file).use { fos ->
                        val bitmap = adjustBitmapFromEditor(layerManager.createBitmapFromAllLayers(), height, width)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                }

                withContext(Dispatchers.Main) {
                    onSaved()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
        }

    fun deleteTempFiles(context: Context) {
        context.deleteFile(MASK_TEXTURE_FILE_NAME)

        val framesDir = File(context.filesDir, MASK_FRAMES_DIR_NAME)
        if (framesDir.exists()) {
            val files = framesDir.listFiles()
            files?.forEach { file ->
                file.delete()
            }
        }
    }
}

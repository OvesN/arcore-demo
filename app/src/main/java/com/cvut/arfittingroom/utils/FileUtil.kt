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
 * Utility class for handling file operations
 *
 * @author Veronika Ovsyannikova
 */
object FileUtil {

    /**
     * Saves the given bitmap as a mask texture file
     *
     * @param bitmap The bitmap to save
     * @param context
     * @param onSaved A callback function to be executed after the bitmap is saved
     */
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

    /**
     * Retrieves the mask texture bitmap from the application's internal storage
     *
     * @param context
     * @return The bitmap if it exists, null otherwise.
     */
    fun getTempMaskTextureBitmap(context: Context): Bitmap? =
        try {
            val fis: FileInputStream = context.openFileInput(MASK_TEXTURE_FILE_NAME)

            val bitmap: Bitmap = BitmapFactory.decodeStream(fis)

            fis.close()

            bitmap
        } catch (e: Exception) {
            null
        }

    /**
     * Retrieves a file input stream for the mask texture file
     *
     * @param context
     * @return The file input stream if it exists, null otherwise
     */
    fun getTempMaskTextureStream(context: Context): FileInputStream? =
        try {
            context.openFileInput(MASK_TEXTURE_FILE_NAME)
        } catch (e: Exception) {
            null
        }

    /**
     * Checks if any temporary animated mask frames are
     * stored in the internal storage
     *
     * @param context
     * @return True if frames exist, false otherwise
     */
    fun doesTempAnimatedMaskExist(context: Context): Boolean {
        val imagesDir = File(context.filesDir, MASK_FRAMES_DIR_NAME)

        return imagesDir.exists() && imagesDir.isDirectory && imagesDir.list()?.isNotEmpty() == true
    }

    /**
     * Retrieves the number of mask frames available
     *
     * @param context
     * @return The number of frames available
     */
    fun getNumberOfFrames(context: Context): Int {
        val imagesDir = File(context.filesDir, MASK_FRAMES_DIR_NAME)

        return imagesDir.list()?.size ?: 0
    }

    /**
     * Retrieves the next mask frame as a bitmap
     *
     * @param context
     * @param counter The frame number to retrieve
     * @return The bitmap if it exists, null otherwise
     */
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

    /**
     * Retrieves a file input stream for the next mask frame
     *
     * @param context
     * @param counter The frame number to retrieve
     * @return The file input stream if it exists, null otherwise
     */
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

    /**
     * Saves the mask frames
     *
     * @param layerManager To get layers
     * @param height The height of the frames
     * @param width The width of the frames
     * @param context
     * @param onSaved A callback function to be executed after the frames are saved
     */
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

    /**
     * Deletes all mask files from the internal file storage
     *
     * @param context
     */
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

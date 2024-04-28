package cz.cvut.arfittingroom.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import cz.cvut.arfittingroom.draw.service.LayerManager
import cz.cvut.arfittingroom.model.MASK_FRAMES_DIR_NAME
import cz.cvut.arfittingroom.model.MASK_FRAME_FILE_NAME
import cz.cvut.arfittingroom.model.MASK_TEXTURE_FILE_NAME
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

object FileUtil {
    fun saveTempMaskTextureBitmap(bitmap: Bitmap, context: Context, onSaved: () -> Unit) {
        try {
            context.openFileOutput(MASK_TEXTURE_FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                onSaved()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTempMaskTextureBitmap(context: Context): Bitmap? {
        return try {
            val fis: FileInputStream = context.openFileInput(MASK_TEXTURE_FILE_NAME)

            val bitmap: Bitmap = BitmapFactory.decodeStream(fis)

            fis.close()

            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun doesTempAnimatedMaskExist(context: Context): Boolean {
        val imagesDir = File(context.filesDir, MASK_FRAMES_DIR_NAME)

        return imagesDir.exists() && imagesDir.isDirectory && imagesDir.list()?.isNotEmpty() == true
    }

    fun getNextTempMaskFrame(context: Context, counter: Int): Bitmap? {
        return try {
            val file = File(
                context.filesDir,
                "${MASK_FRAMES_DIR_NAME}/${MASK_FRAME_FILE_NAME}_$counter.png"
            )

            val fis = FileInputStream(file)

            val bitmap: Bitmap = BitmapFactory.decodeStream(fis)

            fis.close()

            bitmap
        } catch (e: Exception) {
            null
        }
    }


    fun saveTempMaskFrames(
        layerManager: LayerManager,
        height: Int,
        width: Int,
        context: Context,
        onSaved: () -> Unit
    ) {

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
                    val bitmap =
                        adjustBitmap(layerManager.createBitmapFromAllLayers(), height, width)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
            }
            onSaved()
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun adjustBitmap(bitmap: Bitmap, height: Int, width: Int): Bitmap {
        // Calculate the dimensions for the square crop
        val newY = (height - width) / 2

        // Crop the bitmap
        val croppedBitmap = Bitmap.createBitmap(bitmap, 0, newY, width, width)

        // Create a matrix for the mirroring transformation
        val matrix = Matrix().apply {
            postScale(
                -1f,
                1f,
                croppedBitmap.width / 2f,
                croppedBitmap.height / 2f
            )
        }

        // Create and return the mirrored bitmap
        val mirroredBitmap = Bitmap.createBitmap(
            croppedBitmap,
            0,
            0,
            croppedBitmap.width,
            croppedBitmap.height,
            matrix,
            true
        )


        return Bitmap.createScaledBitmap(mirroredBitmap, 1024, 1024, true)
    }

}
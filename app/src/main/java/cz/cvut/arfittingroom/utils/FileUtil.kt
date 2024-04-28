package cz.cvut.arfittingroom.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import cz.cvut.arfittingroom.draw.service.LayerManager
import cz.cvut.arfittingroom.model.MASK_GIF_FILE_NAME
import cz.cvut.arfittingroom.model.MASK_TEXTURE_FILE_NAME
import cz.cvut.arfittingroom.service.AnimatedGifEncoder
import pl.droidsonroids.gif.GifDrawable
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.lang.Exception

object FileUtil {
    private val animatedGifEncoder = AnimatedGifEncoder()
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

    fun getTempMaskGif(context: Context): GifDrawable? {
        return try {
            val fis = context.openFileInput(MASK_GIF_FILE_NAME)
            val bis = BufferedInputStream(fis)
            val gif = GifDrawable(bis)

            fis.close()
            bis.close()

            gif
        } catch (e: Exception) {
            null
        }
    }

    fun saveTempMaskGif(
        layerManager: LayerManager,
        gifDrawable: GifDrawable,
        height: Int,
        width: Int,
        context: Context,
        onSaved: () -> Unit
    ) {
        try {
            context.openFileOutput(MASK_GIF_FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                val gifData = generateGIF(layerManager, height, width, gifDrawable)
                fos.write(gifData)
                fos.close()
                onSaved()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateGIF(
        layerManager: LayerManager,
        height: Int,
        width: Int,
        gifDrawable: GifDrawable
    ): ByteArray {
        val bos = ByteArrayOutputStream()
        val maxNumberOfFrames = layerManager.getMaxNumberOfFrames()

        animatedGifEncoder.start(bos)
        animatedGifEncoder.setDelay(1000)
        animatedGifEncoder.setTransparent(Color.TRANSPARENT)
        animatedGifEncoder.setRepeat(0)
        animatedGifEncoder.setDispose(2)

        var counter = 0
        repeat(maxNumberOfFrames) {
            animatedGifEncoder.addFrame(
                gifDrawable.seekToFrameAndGet(counter)

            )
            counter++
        }

        animatedGifEncoder.finish()
        return bos.toByteArray()
    }


    fun deleteTempFiles(context: Context) {
        context.deleteFile(MASK_TEXTURE_FILE_NAME)
        context.deleteFile(MASK_GIF_FILE_NAME)
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
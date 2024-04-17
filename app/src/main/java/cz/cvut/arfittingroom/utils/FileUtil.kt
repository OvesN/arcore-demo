package cz.cvut.arfittingroom.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cz.cvut.arfittingroom.draw.model.element.impl.Gif
import cz.cvut.arfittingroom.model.MASK_GIF_FILE_NAME
import cz.cvut.arfittingroom.model.MASK_TEXTURE_FILE_NAME
import cz.cvut.arfittingroom.service.AnimatedGifEncoder
import pl.droidsonroids.gif.GifDrawable
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
            val fis: FileInputStream = context.openFileInput(MASK_GIF_FILE_NAME)

            val gif = GifDrawable(fis)

            fis.close()

            gif
        } catch (e: Exception) {
            null
        }
    }

    fun saveTempMaskGif(bitmap: Bitmap, context: Context, onSaved: () -> Unit) {
        try {
            context.openFileOutput(MASK_GIF_FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                onSaved()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO test this
    fun generateGIF(
        staticLayerBitmaps: List<Bitmap>,
        gifByLayerIndex: Map<Int, List<Gif>>
    ): ByteArray? {
        val numberOfFramesList = gifByLayerIndex.values.flatMap { layer ->
            layer.map { it.gifDrawable.numberOfFrames }
        }
        val lcm = lcm(numberOfFramesList)

        val bos = ByteArrayOutputStream()
        animatedGifEncoder.start(bos)

        repeat(lcm) {
           // animatedGifEncoder.addFrame(bitmap)
        }

        animatedGifEncoder.finish()
        return bos.toByteArray()
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }

    private fun lcm(a: Int, b: Int): Int {
        return a / gcd(a, b) * b
    }

    private fun lcm(values: List<Int>): Int {
        if (values.isEmpty()) throw IllegalArgumentException("List should not be empty")
        var result = values[0]
        for (i in 1 until values.size) {
            result = lcm(result, values[i])
            if (result == 0) break
        }
        return result
    }


    fun deleteTempFiles(context: Context) {
        context.deleteFile(MASK_TEXTURE_FILE_NAME)
        context.deleteFile(MASK_GIF_FILE_NAME)
    }

}
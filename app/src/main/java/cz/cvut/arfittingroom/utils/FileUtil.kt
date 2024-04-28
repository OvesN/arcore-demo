package cz.cvut.arfittingroom.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import cz.cvut.arfittingroom.draw.Layer
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
        layers: List<Layer>
    ): ByteArray? {
        var bitmap = Bitmap.createBitmap(2048, 2048, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
       // val maxNumberOfFrames = gifByLayerIndex.values.flatMap { it.flatMap { maxOf(it.gifDrawable.numberOfFrames) }}
        layers.forEach {

//            if(it is Gif) {
//
//            }
        }
        val bos = ByteArrayOutputStream()
        animatedGifEncoder.start(bos)


         // animatedGifEncoder.addFrame(bitmap)


        animatedGifEncoder.finish()
        return bos.toByteArray()
    }
    

    fun deleteTempFiles(context: Context) {
        context.deleteFile(MASK_TEXTURE_FILE_NAME)
        context.deleteFile(MASK_GIF_FILE_NAME)
    }

}
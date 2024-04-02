package cz.cvut.arfittingroom.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cz.cvut.arfittingroom.model.MASK_TEXTURE_FILE_NAME
import java.io.FileInputStream
import java.lang.Exception

object FileSavingUtil {
    fun saveTempMaskTextureBitmap(bitmap: Bitmap, context: Context, onSaved: () -> Unit) {
        try {
            context.openFileOutput(MASK_TEXTURE_FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                // Notify that the bitmap is saved
                onSaved()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTempMaskTextureBitmap(context: Context): Bitmap? {
        return try {
            // Open an input stream to the file.
            val fis: FileInputStream = context.openFileInput(MASK_TEXTURE_FILE_NAME)

            // Decode the bitmap from the input stream.
            val bitmap: Bitmap = BitmapFactory.decodeStream(fis)

            // Close the file input stream.
            fis.close()

            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun deleteTempMaskTextureBitmap(context: Context) {
        val fileDeleted = context.deleteFile(MASK_TEXTURE_FILE_NAME)
    }

}
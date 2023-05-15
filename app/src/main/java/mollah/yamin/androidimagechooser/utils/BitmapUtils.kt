package mollah.yamin.androidimagechooser.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.util.Log
import java.io.FileInputStream
import java.io.IOException

object BitmapUtils {
    @Throws(IOException::class)
    fun getCorrectlyOrientedImage(imagePath: String): Bitmap {
        val options = BitmapFactory.Options()
        var srcBitmap: Bitmap = BitmapFactory.decodeStream(
            FileInputStream(imagePath), null, options
        ) ?: throw IOException()
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        Log.d("EXIF", "Exif: $orientation")
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.postRotate(270f)
            }
        }
        srcBitmap = Bitmap.createBitmap(
            srcBitmap,
            0,
            0,
            srcBitmap.width,
            srcBitmap.height,
            matrix,
            true
        ) // rotating bitmap
        return srcBitmap
    }
}
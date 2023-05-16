package mollah.yamin.androidimagechooser.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


object BitmapUtils {

    // Corrects image rotation
    @Throws(IOException::class)
    fun getCorrectlyOrientedImage(imagePath: String): Bitmap {
        val srcBitmap: Bitmap = getBitmapFromFilePath(imagePath) ?: throw IOException()
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        var rotationDegrees = 0
        var flipX = false
        var flipY = false
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipX = true
            ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                rotationDegrees = 90
                flipX = true
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees =
                180
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipY = true
            ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees =
                -90
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                rotationDegrees = -90
                flipX = true
            }
            ExifInterface.ORIENTATION_UNDEFINED, ExifInterface.ORIENTATION_NORMAL -> {
            }
            else -> {
            }
        }
        return rotateBitmap(srcBitmap, rotationDegrees, flipX, flipY)
    }

    /**
     * Rotates a bitmap if it is converted from a bytebuffer.
     */
    fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    // Resize image resolution
    // Use maxSize 500 to get medium quality image with faster conversion
    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        if (maxSize < 1) return image
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    // Compress image size
    // Highest quality is 100
    fun compressBitmap(
        imageBitmap: Bitmap,
        imageQuality: Int,
        path: String
    ): File {
        val outputFile = File(path)
        try {
            val fos = FileOutputStream(outputFile)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputFile
    }

    fun getBitmapFromFilePath(path: String): Bitmap? =
        BitmapFactory.decodeStream(FileInputStream(path), null, BitmapFactory.Options())

    /**
     *
     * @param bmp input bitmap
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new Bitmap
     */
    fun changeBitmapContrastBrightness(bmp: Bitmap, contrast: Float, brightness: Float): Bitmap? {
        val outBitmap = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)
        val canvas = Canvas(outBitmap)
        val paint = Paint()
        paint.colorFilter = getContrastBrightnessFilter(contrast, brightness)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        return outBitmap
    }

    /**
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return ColorMatrixColorFilter
     */
    fun getContrastBrightnessFilter(contrast: Float, brightness: Float): ColorMatrixColorFilter {
        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return ColorMatrixColorFilter(cm)
    }
}
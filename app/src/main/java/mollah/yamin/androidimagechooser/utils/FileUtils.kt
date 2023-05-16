package mollah.yamin.androidimagechooser.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import mollah.yamin.androidimagechooser.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    // Checks if a volume containing external storage is available
    // for read and write.
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // Checks if a volume containing external storage is available to at least read.
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
    fun makeEmptyFolderIntoExternalStorageWithTitle(applicationContext: Context, folderName: String): Boolean {
        val folderPath = getLocalStorageFolderPath(applicationContext, folderName)
        val folder = File(folderPath)
        if (!folder.exists()) {
            if (!folder.mkdir())
                return false
        }
        return true
    }

    @Throws(IOException::class)
    fun createImageFileInImageDir(activity: AppCompatActivity): File {
        // Create a unique image file name
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(Date())

        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    fun makeEmptyFileIntoExternalStorageWithTitle(applicationContext: Context, folderName: String, fileName: String): File {
        //If your app is used on a device that runs Android 4.3 (API level 18) or lower,
        // then the array contains just one element,
        // which represents the primary external storage volume
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(applicationContext, null)
        val primaryExternalStorage = externalStorageVolumes[0]
        //path = "$primaryExternalStorage/$realDocId"

        //val root: String = Environment.getExternalStorageDirectory().getAbsolutePath()
        return if (folderName == "")
            File(primaryExternalStorage.absolutePath, fileName)
        else
            File("${primaryExternalStorage.absolutePath}/$folderName", fileName)
    }

    @Suppress("unused")
    @Throws(Exception::class)
    fun saveBitmapFileIntoExternalStorageWithTitle(
        applicationContext: Context,
        bitmap: Bitmap,
        folderName: String,
        fileName: String
    ) {
        val fileOutputStream =
            FileOutputStream(makeEmptyFileIntoExternalStorageWithTitle(applicationContext, folderName, fileName))
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.close()
    }

    private fun getLocalStorageFolderPath(applicationContext: Context, folderName: String): String {
        //If your app is used on a device that runs Android 4.3 (API level 18) or lower,
        // then the array contains just one element,
        // which represents the primary external storage volume
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(
            applicationContext,
            null
        )
        val primaryExternalStorage = externalStorageVolumes[0]

        return if (folderName == "")
            primaryExternalStorage.absolutePath
        else
            "${primaryExternalStorage.absolutePath}/$folderName"
    }

    fun deleteFolderWithAllFilesFromExternalStorage(applicationContext: Context, folderName: String): Boolean {
        val folderPath = getLocalStorageFolderPath(applicationContext, folderName)
        val folder = File(folderPath)
        if (folder.exists()) {
            if (!folder.deleteRecursively())
                return false
        }
        return true
    }

    fun getFilePathFromMediaContentUri(uri: Uri, context: Context): String? {
        val mediaStorePath = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(
            uri, mediaStorePath,
            null, null, null
        ) ?: return null
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(mediaStorePath[0])
        val imagePath = cursor.getString(columnIndex)
        cursor.close()
        return imagePath
    }

    fun deleteFolderFromExternalStorage(file: File): Boolean {
        if (file.exists()) {
            if (!file.deleteRecursively())
                return false
        }
        return true
    }

    fun formatFileSizeInText(size: Double): String {
        if (size < 1024) {
            return "${size.toRounded(1)} B"
        }
        return fileSizeInKB(size)
    }

    private fun fileSizeInKB(size: Double): String {
        val value = size / 1024
        if (value < 1024) {
            return "${value.toRounded(1)} KB"
        }
        return fileSizeInMB(value)
    }

    private fun fileSizeInMB(size: Double): String {
        val value = size / 1024
        if (value < 1024) {
            return "${value.toRounded(1)} MB"
        }
        return fileSizeInGB(size)
    }

    private fun fileSizeInGB(size: Double): String {
        return "${(size/1024).toRounded(1)} GB"
    }
}
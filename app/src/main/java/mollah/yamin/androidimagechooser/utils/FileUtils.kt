package mollah.yamin.androidimagechooser.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

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
}
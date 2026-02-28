package com.pl.myweightapp.data

import android.content.Context
import android.util.Log
import com.pl.myweightapp.domain.StorageSupport
import java.io.File
import java.io.InputStream

class StorageSupportImpl(
    private val context: Context,
) : StorageSupport {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    override fun exists(path: String): Boolean = File(path).exists()

    override fun logStorage() {
        (context.filesDir.listFiles() ?: emptyArray()).forEach { file ->
            Log.d(
                TAG,
                "File[filesDir]: ${file.name}, size: ${file.length()}, exists: ${file.exists()}"
            )
        }
        (context.cacheDir.listFiles() ?: emptyArray()).forEach { file ->
            Log.d(
                TAG,
                "File[cacheDir]: ${file.name}, size: ${file.length()}, exists: ${file.exists()}"
            )
        }
    }

    override fun cleanupTemporary() {
    }

    override fun saveProfileImage(input: InputStream): String {
        val file = File(context.cacheDir, "profile_tmp_gallery.jpg")
        input.use { it.copyTo(file.outputStream()) }
        return file.absolutePath
    }

    override fun moveTmpToFinal(fromPath: String, toFilename: String): String {
        val finalFile = File(context.filesDir, toFilename)
        val tmpFile = File(fromPath)

        tmpFile.copyTo(finalFile, overwrite = true)

        Log.d(TAG, "Copied tmp file to: ${finalFile.absolutePath}")
        Log.d(TAG, "Delete tmp file: ${tmpFile.absolutePath}")

        return finalFile.absolutePath
    }


/*
fun saveUriToInternalFile(uri: Uri): String {
    val context = getApplication() as Context
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File(context.filesDir, "profile_photo.jpg")
    inputStream.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}
*/


}
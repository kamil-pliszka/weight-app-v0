package com.pl.myweightapp.data

import android.content.Context
import android.util.Log
import com.pl.myweightapp.domain.StorageSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class StorageSupportImpl(
    private val context: Context,
) : StorageSupport {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    override suspend fun exists(path: String): Boolean = withContext(Dispatchers.IO) {
        File(path).exists()
    }

    override suspend fun logStorage() = withContext(Dispatchers.IO) {
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

    override suspend fun cleanupTemporary() = withContext(Dispatchers.IO) {
    }

    override suspend fun saveProfileImage(input: InputStream): String = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "profile_tmp_gallery.jpg")
        input.use { it.copyTo(file.outputStream()) }
        file.absolutePath
    }

    override suspend fun copyTmpToFinal(fromPath: String, toFilename: String): String = withContext(Dispatchers.IO) {
        val finalFile = File(context.filesDir, toFilename)
        val tmpFile = File(fromPath)

        tmpFile.copyTo(finalFile, overwrite = true)

        Log.d(TAG, "Copied tmp file to: ${finalFile.absolutePath}")

        finalFile.absolutePath
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
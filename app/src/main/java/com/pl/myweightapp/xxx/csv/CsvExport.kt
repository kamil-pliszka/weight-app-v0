package com.pl.myweightapp.xxx.csv

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.pl.myweightapp.AppModule
import com.pl.myweightapp.repositories.sortWeightMeasureHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "CsvExport"
suspend fun exportWeightCsv(
    context: Context,
    uri: Uri,
    onProgressChange: (Float) -> Unit
): Int = withContext(Dispatchers.IO) {
    val repo = AppModule.provideWeightMeasureRepository()
    val historyEntities = sortWeightMeasureHistory(repo.findWeightMeasureHistory())
    Log.d(TAG,"history entities : ${historyEntities.size}")
    context.contentResolver.openOutputStream(uri)?.use { output ->
        val writer = output.bufferedWriter()
        writer.write("#Weight Date,Weight Measurement,Weight Unit\n")
        historyEntities.forEachIndexed { idx, entity ->
            val line =
                "${entity.date},${entity.weight.toPlainString()},${entity.unit.name.lowercase()}"
            writer.write(line)
            writer.write("\n")
            onProgressChange((idx + 1).toFloat() / historyEntities.size)
        }
        writer.flush()
    }
    historyEntities.size //return
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
    }
    // Fallback (np. file://)
    return uri.path?.substringAfterLast('/')
}
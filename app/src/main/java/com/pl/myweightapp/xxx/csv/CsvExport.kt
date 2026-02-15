package com.pl.myweightapp.xxx.csv

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.pl.myweightapp.AppModule
import com.pl.myweightapp.repositories.sortWeightMeasureHistory

suspend fun exportWeightCsv(
    context: Context,
    uri: Uri,
    onProgressChange: (Float) -> Unit
): Int {
    val repo = AppModule.provideWeightMeasureRepository()
    val historyEntities = sortWeightMeasureHistory(repo.findWeightMeasureHistory())
    println("history entities : ${historyEntities.size}")
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
    return historyEntities.size
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
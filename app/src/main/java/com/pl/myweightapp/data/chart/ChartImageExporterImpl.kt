package com.pl.myweightapp.data.chart

import android.content.Context
import android.util.Log
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.domain.chart.ChartImage
import com.pl.myweightapp.domain.chart.ChartImageExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ChartImageExporterImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ChartImageExporter {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    override suspend fun export(
        image: ChartImage
    ) {
        Log.d(TAG, "Exporting chart")
        val file = saveImageToFileAsync(
            context,
            image.bytes,
            Constants.WEIGHT_CHART_FILENAME
        )
        Log.d(TAG, "Zapisano wykres do: ${file.absolutePath}")
    }

    private suspend fun saveImageToFileAsync(
        context: Context,
        bytes: ByteArray,
        fileName: String
    ): File = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, fileName) // lub context.cacheDir
        FileOutputStream(file).use { out ->
            out.write(bytes)
            //bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        file
    }

}
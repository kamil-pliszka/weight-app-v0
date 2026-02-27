package com.pl.myweightapp.data.chart

import android.content.Context
import android.util.Log
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.domain.chart.ChartImage
import com.pl.myweightapp.domain.chart.ChartImageImporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ChartImageImporterImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ChartImageImporter {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    override suspend fun import() : ChartImage? {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "tryToLoadFromFile")
            val file = File(context.filesDir, Constants.WEIGHT_CHART_FILENAME)
            loadImageFromFile(file)
        }
    }

    private fun loadImageFromFile(file: File): ChartImage? {
        if (!file.exists()) return null
        return ChartImage(file.readBytes())
        //return BitmapFactory.decodeFile(file.absolutePath)
    }
}
package com.pl.myweightapp.domain.usecase

import android.util.Log
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartImage
import com.pl.myweightapp.feature.home.chart.ChartRenderer
import javax.inject.Inject

class GenerateChartImageUseCase @Inject constructor(
    private val chartRenderer: ChartRenderer,
) {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    operator suspend fun invoke(
        chartData: ChartData,
        widthPx: Int,
        heightPx: Int
    ) : ChartImage? {
        Log.d(TAG, "Generate chart ${widthPx}x${heightPx}")
        Log.d(TAG, "Measures: ${chartData.measures.size}")

        if (chartData.measures.isNotEmpty() && widthPx > 0 && heightPx > 0) {
            // Renderer zwraca już ChartImage (ByteArray)
            val chartImage = chartRenderer.render(
                chartData = chartData,
                widthPx = widthPx,
                heightPx = heightPx,
            )
            return chartImage
        } else {
            return null
        }
    }

}
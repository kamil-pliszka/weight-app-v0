package com.pl.myweightapp.feature.home.chart

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartImage
import com.pl.myweightapp.domain.chart.ChartLabels
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class MpChartBitmapRenderer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ChartRenderer {

    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    override suspend fun render(
        chartData: ChartData,
        chartLabels: ChartLabels,
        widthPx: Int,
        heightPx: Int
    ): ChartImage {
        val chart = LineChart(context).apply {
            setTouchEnabled(false)
            setDragEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        chart.legend.apply {
            isEnabled = true
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            //yOffset = 4f
        }
        chart.axisRight.isEnabled = true
        chart.description.isEnabled = false

        configureChart(
            chart,
            chartData,
            chartLabels,
            null,
        )

        Log.d(TAG, "periodOnChartDays : ${chartData.periodOnChart}")
        val isLandscape = widthPx > heightPx
        val width = if (isLandscape) {
            (chartData.periodOnChart * 3.0 * widthPx / 365).toInt().coerceIn(widthPx, 4 * widthPx)
        } else {
            (chartData.periodOnChart * 3.0 * widthPx / 365).toInt().coerceIn(widthPx, 6 * widthPx)
        }
        Log.d(TAG, "chart size: ${width}x$heightPx")
        chart.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
        )
        chart.layout(0, 0, width, heightPx)
        val bitmap = chart.chartBitmap

        Log.d(TAG, "Bitmap: ${bitmap.width}x${bitmap.height}")

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        return ChartImage(bytes)
    }
}
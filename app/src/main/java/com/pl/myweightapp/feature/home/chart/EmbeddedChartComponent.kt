package com.pl.myweightapp.feature.home.chart

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.pl.myweightapp.feature.home.HomeScreenUiState

@Composable
fun EmbeddedChartComponent(
    state: HomeScreenUiState,
) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            LineChart(context).apply {
                setTouchEnabled(true)
                setDragEnabled(true)
                setScaleEnabled(true)
                setPinchZoom(false)

                description.isEnabled = false
                axisRight.isEnabled = true
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            }
        },
        update = { chart ->
            configureChart(
                context = context,
                chart = chart,
                chartData = state.chartData,
                maxLabels = 6,
            )
            chart.invalidate()
        }
    )
}
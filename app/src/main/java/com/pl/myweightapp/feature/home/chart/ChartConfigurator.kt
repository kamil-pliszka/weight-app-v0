package com.pl.myweightapp.feature.home.chart

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.pl.myweightapp.core.util.daysToMillis
import com.pl.myweightapp.core.util.millisToDaysFloat
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartLabels
import com.pl.myweightapp.domain.chart.ChartMeasure
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "ChartConfigurator"

fun configureChart(
    chart: LineChart,
    chartData: ChartData,
    chartLabels: ChartLabels,
    maxLabels: Int? = null,
) {
    Log.d(TAG, "start")

    if (chartData.measures.isEmpty()) {
        chart.clear()
        return
    }
    val startMillis = chartData.measures.first().timestamp
    val entriesOnChart = createEntries(chartData.measures, startMillis)

    configureXAxix(chart.xAxis, startMillis, chartData.periodOnChart, maxLabels)

    val average = chartData.measures.map { it.value }.average().toFloat()
    val dataSets = mutableListOf<ILineDataSet>(
        LineDataSet(entriesOnChart, chartLabels.weightLabel).apply {
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.13f  // zakres 0.05–1.0
        },
        LineDataSet(
            listOf(
                Entry(entriesOnChart.first().x, average),
                Entry(entriesOnChart.last().x, average),
            ),
            chartLabels.averageLabel
        ).apply {
            lineWidth = 1.5f
            setDrawCircles(false)
            setDrawValues(false)
            enableDashedLine(10f, 10f, 0f)
            color = Color.Green.toArgb()
        }
    )
    if (chartData.targetValue != null) {
        dataSets.add(
            LineDataSet(
                listOf(
                    Entry(entriesOnChart.first().x, chartData.targetValue),
                    Entry(entriesOnChart.last().x, chartData.targetValue),
                ),
                chartLabels.targetLabel
            ).apply {
                lineWidth = 1.5f
                setDrawCircles(false)
                setDrawValues(false)
                enableDashedLine(10f, 10f, 0f)
                color = Color.Red.toArgb()
            }
        )
    }
    if (chartData.movingAverage1.isNotEmpty()) {
        Log.d(TAG, "Generate MAV1, period: ${chartData.mav1Period}")
        dataSets.add(
            LineDataSet(
                createEntries(chartData.movingAverage1, startMillis),
                chartLabels.mavPrefixLabel + chartData.mav1Period
            ).apply {
                lineWidth = 1.5f
                setDrawCircles(false)
                setDrawValues(false)
                enableDashedLine(10f, 10f, 0f)
                color = Color.Gray.toArgb()
            }
        )
    }
    if (chartData.movingAverage2.isNotEmpty()) {
        Log.d(TAG, "Generate MAV2, period: ${chartData.mav2Period}")
        dataSets.add(
            LineDataSet(
                createEntries(chartData.movingAverage2, startMillis),
                chartLabels.mavPrefixLabel + chartData.mav2Period
            ).apply {
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                enableDashedLine(20f, 20f, 0f)
                color = Color.DarkGray.toArgb()
            }
        )
    }

    chart.data = LineData(dataSets.toList())

}

private fun configureXAxix(
    xAxis: XAxis, startMillis: Long,
    periodOnChartDays: Float, maxLabels: Int?
) {
    xAxis.apply {
        valueFormatter = when {
            (periodOnChartDays < 100) -> DateAxisFormatterShort(startMillis)
            else -> DateAxisFormatterMedium(startMillis)
        }
        //textSize = 12f      // jeśli generujesz bitmapę 1600px wysokości
        yOffset = 6f
        //labelRotationAngle = -45f
        //centerAxisLabels
        //setCenterAxisLabels(true)
        if (periodOnChartDays < 100) {
            setLabelCount(5, true)
            Log.d(TAG, "Set labelCount = 5")
        } else /*if (periodOnChartDays < 300)*/ {
            setLabelCount((periodOnChartDays / 30).toInt().coerceIn(null, maxLabels), true)
            Log.d(TAG, "Set labelCount to: ${(periodOnChartDays / 30).toInt()} -> $labelCount")
        }
    }
}

fun createEntries(measurements: List<ChartMeasure>, startMillis: Long): List<Entry> {
    return measurements.map { m -> //Measurement
        Entry(
            //(m.timestamp.toEpochMilli() - startMillis).toFloat(),
            (m.timestamp - startMillis).millisToDaysFloat(),
            m.value
        )
    }
}

class DateAxisFormatterShort(
    private val startMillis: Long,
) : ValueFormatter() {
    private val formatterShort = DateTimeFormatter.ofPattern("dd.MM")
        .withZone(ZoneId.systemDefault())

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val millis = startMillis + value.daysToMillis()
        val instant = Instant.ofEpochMilli(millis)
        //Log.d(TAG,"getAxisLabel : $instant")

        return formatterShort.format(instant)
    }
}

class DateAxisFormatterMedium(
    private val startMillis: Long,
) : ValueFormatter() {

    private val formatterMedium = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(ZoneId.systemDefault())

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val millis = startMillis + value.daysToMillis()
        val instant = Instant.ofEpochMilli(millis)
        //Log.d(TAG,"getAxisLabel : $instant")
        return formatterMedium.format(instant)
    }
}


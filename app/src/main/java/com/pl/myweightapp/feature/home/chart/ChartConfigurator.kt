package com.pl.myweightapp.feature.home.chart

import android.content.Context
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
import com.pl.myweightapp.R
import com.pl.myweightapp.core.util.daysToMillis
import com.pl.myweightapp.core.util.millisToDaysFloat
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartMeasure
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun configureChart(
    context: Context,
    chart: LineChart,
    chartData: ChartData,
    maxLabels: Int? = null,
) {
    if (chartData.measures.isEmpty()) {
        chart.clear()
        return
    }
    val startMillis = chartData.measures.first().timestamp
    val entriesOnChart = createEntries(chartData.measures, startMillis)

    configureXAxis(chart.xAxis, startMillis, chartData.periodOnChart, maxLabels)

    val dataSets = mutableListOf<ILineDataSet>(
        LineDataSet(entriesOnChart, context.getString(R.string.chart_weight)).apply {
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.13f  // zakres 0.05–1.0
        },
    )
    if (chartData.average != null) {
        dataSets.add(
            LineDataSet(
                listOf(
                    Entry(entriesOnChart.first().x, chartData.average),
                    Entry(entriesOnChart.last().x, chartData.average),
                ),
                context.getString(R.string.chart_average)
            ).apply {
                lineWidth = 1.5f
                setDrawCircles(false)
                setDrawValues(false)
                enableDashedLine(10f, 10f, 0f)
                color = Color.Green.toArgb()
            }
        )
    }
    if (chartData.targetValue != null) {
        dataSets.add(
            LineDataSet(
                listOf(
                    Entry(entriesOnChart.first().x, chartData.targetValue),
                    Entry(entriesOnChart.last().x, chartData.targetValue),
                ),
                context.getString(R.string.chart_target)
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
        dataSets.add(
            LineDataSet(
                createEntries(chartData.movingAverage1, startMillis),
                context.getString(R.string.chart_mav) + chartData.mav1Period
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
        dataSets.add(
            LineDataSet(
                createEntries(chartData.movingAverage2, startMillis),
                context.getString(R.string.chart_mav) + chartData.mav2Period
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

private fun configureXAxis(
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
        } else /*if (periodOnChartDays < 300)*/ {
            val calculated = (periodOnChartDays / 30).toInt()
            val labelCount = maxLabels?.let { calculated.coerceAtMost(it) } ?: calculated
            setLabelCount(labelCount, true)
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
        return formatterMedium.format(instant)
    }
}


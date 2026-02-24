package com.pl.myweightapp.feature.home.chart

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.pl.myweightapp.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Measurement(
    val timestamp: Instant,
    val value: Float
)

fun Long.millisToDaysFloat(): Float {
    return (this / 86_400_000.0).toFloat()
}

fun Float.daysToMillis(): Long {
    return kotlin.math.round(this * 86_400_000.0).toLong()
}

private const val TAG = "ChartGenerator"
fun generateChartBitmap(
    context: Context,
    totalMeasurements: List<Measurement>,//muszą już być posortowane w kolejności rosnąco
    startIdx: Int,
    widthPx: Int, heightPx: Int,
    targetValue: Float?,
    movingAverage1: Int? = null,
    movingAverage2: Int? = null,
): Bitmap {

    val chart = LineChart(context).apply {
        setTouchEnabled(false)
        setDragEnabled(false)
        setScaleEnabled(false)
        setPinchZoom(false)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    chart.legend.apply {
        isEnabled = true
        verticalAlignment = LegendVerticalAlignment.TOP
        horizontalAlignment = LegendHorizontalAlignment.RIGHT
        //yOffset = 4f
    }
    chart.axisRight.isEnabled = true
    chart.description.isEnabled = false

    configureChart(
        context,
        chart,
        totalMeasurements,
        startIdx,
        targetValue,
        movingAverage1,
        movingAverage2
    )

//    val padding = Resources.getSystem().displayMetrics.density * (extPadding?:0)
//    Log.d(TAG,"image padding: $padding")
//    val screenWidth = Resources.getSystem().displayMetrics.widthPixels//-padding.toInt()
//    val screenHeight = Resources.getSystem().displayMetrics.heightPixels
//    Log.d(TAG,"screen size: ${screenWidth}x$screenHeight")
//    Log.d(TAG,"screen size w.padding: ${Resources.getSystem().displayMetrics.widthPixels-padding.toInt()}x${Resources.getSystem().displayMetrics.heightPixels-padding.toInt()}")
    val measurementsOnChart = totalMeasurements.subList(startIdx, totalMeasurements.size)
    if (measurementsOnChart.isEmpty()) return chart.chartBitmap
    val periodOnChartDays = (measurementsOnChart.last().timestamp.toEpochMilli() -
            measurementsOnChart.first().timestamp.toEpochMilli()).millisToDaysFloat()
    Log.d(TAG, "periodOnChartDays : $periodOnChartDays")
    val isLandscape = widthPx > heightPx
    val height = heightPx
    val width = if (isLandscape) {
        (periodOnChartDays * 3.0 * widthPx / 365).toInt().coerceIn(widthPx, 4 * widthPx)
    } else {
        (periodOnChartDays * 3.0 * widthPx / 365).toInt().coerceIn(widthPx, 6 * widthPx)
    }
    Log.d(TAG, "chart size: ${width}x$height")
    chart.measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
    )

    chart.layout(0, 0, width, height)

    return chart.chartBitmap
}


fun configureChart(
    context: Context,
    chart: LineChart,
    totalMeasurements: List<Measurement>,//muszą już być posortowane w kolejności rosnąco
    startIdx: Int,
    targetValue: Float?,
    movingAverage1: Int? = null,
    movingAverage2: Int? = null,
    maxLabels: Int? = null,
) {
    Log.d(TAG, "configureChart")
    if (totalMeasurements.isEmpty()) {
        chart.clear()
        return
    }
    val measurementsOnChart = totalMeasurements.subList(startIdx, totalMeasurements.size)
    val startMillis = measurementsOnChart.first().timestamp.toEpochMilli()
    val periodOnChartDays = (measurementsOnChart.last().timestamp.toEpochMilli() -
            measurementsOnChart.first().timestamp.toEpochMilli()).millisToDaysFloat()
    Log.d(TAG, "periodOnChartDays : $periodOnChartDays")
    val totalEntries = createEntries(totalMeasurements, startMillis)
    val entries = totalEntries.subList(startIdx, totalEntries.size)
    for (i in 1 until totalEntries.size) {
        check(totalEntries[i].x >= totalEntries[i - 1].x) {
            "Entries must be sorted by x ascending"
        }
    }

    chart.xAxis.apply {
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

    val average = measurementsOnChart.map { it.value }.average().toFloat()
    val dataSets = mutableListOf<ILineDataSet>(
        LineDataSet(entries, context.getString(R.string.chart_weight)).apply {
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.13f  // zakres 0.05–1.0
        },
        LineDataSet(
            listOf(
                Entry(entries.first().x, average),
                Entry(entries.last().x, average),
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
    if (targetValue != null) {
        dataSets.add(
            LineDataSet(
                listOf(
                    Entry(entries.first().x, targetValue),
                    Entry(entries.last().x, targetValue),
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
    if (movingAverage1 != null) {
        Log.d(TAG, "Generate MAV1, period: $movingAverage1")
        val mavEntries = generateMovingAverageData(totalEntries, startIdx, movingAverage1.toFloat())
        if (mavEntries.isNotEmpty()) {
            dataSets.add(
                LineDataSet(
                    mavEntries,
                    context.getString(R.string.chart_mav) + movingAverage1
                ).apply {
                    lineWidth = 1.5f
                    setDrawCircles(false)
                    setDrawValues(false)
                    enableDashedLine(10f, 10f, 0f)
                    color = Color.Gray.toArgb()
                }
            )
        }
    }
    if (movingAverage2 != null) {
        Log.d(TAG, "Generate MAV2, period: $movingAverage2")
        val mavEntries = generateMovingAverageData(totalEntries, startIdx, movingAverage2.toFloat())
        if (mavEntries.isNotEmpty()) {
            dataSets.add(
                LineDataSet(
                    mavEntries,
                    context.getString(R.string.chart_mav) + movingAverage2
                ).apply {
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawValues(false)
                    enableDashedLine(20f, 20f, 0f)
                    color = Color.DarkGray.toArgb()
                }
            )
        }
    }

    chart.data = LineData(dataSets.toList())

}

fun createEntries(measurements: List<Measurement>, startMillis: Long): List<Entry> {
    return measurements
        .mapIndexed { _, m -> //Int, Measurement
            Entry(
                //(m.timestamp.toEpochMilli() - startMillis).toFloat(),
                (m.timestamp.toEpochMilli() - startMillis).millisToDaysFloat(),
                m.value
            )
        }
}

/**
 * @mavPeriod - okres
 */
private fun generateMovingAverageData(
    totalEntries: List<Entry>,
    startIdx: Int,
    windowDuration: Float
): List<Entry> {

    if (totalEntries.isEmpty() || windowDuration <= 0f)
        return emptyList()

    val result = ArrayList<Entry>((totalEntries.size - startIdx).coerceAtLeast(0))

    var startWindowIdx = 0
    var sum = 0.0

    for (i in totalEntries.indices) {

        sum += totalEntries[i].y
        val windowStartX = totalEntries[i].x - windowDuration

        while (startWindowIdx < i &&
            totalEntries[startWindowIdx].x < windowStartX
        ) {
            sum -= totalEntries[startWindowIdx].y
            startWindowIdx++
        }

        if (i >= startIdx) {
            val count = i - startWindowIdx + 1
            val avg = (sum / count).toFloat()
            result.add(Entry(totalEntries[i].x, avg))
        }
    }

    return result
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


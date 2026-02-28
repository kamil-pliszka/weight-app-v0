package com.pl.myweightapp.domain.usecase

import com.pl.myweightapp.core.util.daysToMillis
import com.pl.myweightapp.core.util.millisToDaysFloat
import com.pl.myweightapp.domain.WeightMeasure
import com.pl.myweightapp.domain.WeightUnit
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartMeasure
import com.pl.myweightapp.domain.convertWeightTo

class GenerateWeightChartDataUseCase {
    operator fun invoke(
        totalWeightMeasures: List<WeightMeasure>, //muszą już być posortowane w kolejności rosnąco
        unit: WeightUnit,
        startIdx: Int, //TODO - do potencjalnego zastąpienia przez PERIOD
        movingAverage1: Int? = null,
        movingAverage2: Int? = null,
        targetValue: Float? = null,
        ): ChartData {

        validateChartMeasures(totalWeightMeasures)
        val totalMeasures = prepareChartMeasures(totalWeightMeasures, unit)
        val measuresOnChart = totalMeasures.subList(startIdx, totalMeasures.size)
        if (measuresOnChart.isEmpty()) return ChartData()

        val average = measuresOnChart.map { it.value }.average().toFloat()

        val mav1 = movingAverage1?.let {
            generateMovingAverage(
                totalMeasures,
                startIdx,
                movingAverage1.toFloat()
            )
        } ?: emptyList()

        val mav2 = movingAverage2?.let {
            generateMovingAverage(
                totalMeasures,
                startIdx,
                movingAverage2.toFloat()
            )
        } ?: emptyList()

        return ChartData(
            measures = measuresOnChart,
            average = average,
            movingAverage1 = mav1,
            movingAverage2 = mav2,
            mav1Period = movingAverage1,
            mav2Period = movingAverage2,
            targetValue = targetValue,
            periodOnChart = (measuresOnChart.last().timestamp -
                    measuresOnChart.first().timestamp).millisToDaysFloat()
        )
    }
}

private fun validateChartMeasures(measures: List<WeightMeasure>) {
    for (i in 1 until measures.size) {
        check(measures[i].date >= measures[i - 1].date) {
            "Measurements must be sorted by date ascending"
        }
    }
}

fun prepareChartMeasures(
    history: List<WeightMeasure>,
    targetUnit: WeightUnit
): List<ChartMeasure> {
    return history.map { measure ->
        ChartMeasure(
            measure.date.toEpochMilli(),
            measure.convertWeightTo(targetUnit)
        )
    }
}


/**
 * sliding window average
 * totalMeasures są posortowane rosnąco po timestamp
 */
private fun generateMovingAverage(
    totalMeasures: List<ChartMeasure>,
    startIdx: Int,
    durationDays: Float,
): List<ChartMeasure> {
    if (totalMeasures.isEmpty() || durationDays <= 1 || startIdx !in totalMeasures.indices)
        return emptyList()

    val result = ArrayList<ChartMeasure>((totalMeasures.size - startIdx).coerceAtLeast(0))


    val windowDuration = durationDays.daysToMillis()
    val startAvgMillis = totalMeasures[startIdx].timestamp - windowDuration
    val startAvgIdx =
        totalMeasures.indexOfLast { it.timestamp <= startAvgMillis }.coerceIn(0, startIdx)

    var startWindowIdx = startAvgIdx
    var sum = 0.0
    for (j in startAvgIdx until startIdx) {
        sum += totalMeasures[j].value
    }
    for (i in startIdx until totalMeasures.size) {
        sum += totalMeasures[i].value
        val windowStartX = totalMeasures[i].timestamp - windowDuration

        while (startWindowIdx < i &&
            totalMeasures[startWindowIdx].timestamp < windowStartX
        ) {
            sum -= totalMeasures[startWindowIdx].value
            startWindowIdx++
        }

        val count = i - startWindowIdx + 1
        val avg = (sum / count).toFloat()
        result.add(ChartMeasure(totalMeasures[i].timestamp, avg))
    }

    return result
}
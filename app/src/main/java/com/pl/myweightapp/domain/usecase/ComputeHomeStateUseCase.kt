package com.pl.myweightapp.domain.usecase

import android.util.Log
import com.pl.myweightapp.core.util.toInstant
import com.pl.myweightapp.core.util.toLocalDate
import com.pl.myweightapp.domain.AppSettings
import com.pl.myweightapp.domain.DisplayPeriod
import com.pl.myweightapp.domain.WeightMeasure
import com.pl.myweightapp.domain.WeightUnit
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.convertValueTo
import com.pl.myweightapp.domain.convertWeightTo
import java.math.BigDecimal
import javax.inject.Inject

data class HomeComputationResult(
    val unit: WeightUnit,
    val period: DisplayPeriod,
    val movingAverage1: Int?,
    val movingAverage2: Int?,
    val useEmbeddedChart: Boolean,
    val startWeight: Float?,
    val currentWeight: Float?,
    val destinationWeight: Float?,
    val periodWeightChange: Float?,
    val toTargetWeight: Float?,
    val chartData: ChartData
)

class ComputeHomeStateUseCase @Inject constructor(
    private val generateWeightChartDataUseCase: GenerateWeightChartDataUseCase
) {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    operator fun invoke(
        settings: AppSettings,
        measures: List<WeightMeasure>, //tutaj pomiary już posortowane rosnąco względem daty
        weightUnit: WeightUnit?,
        targetWeight: BigDecimal?
    ): HomeComputationResult {

        val measureUnit: WeightUnit = weightUnit ?: WeightUnit.KG
        val period: DisplayPeriod = runCatching {
            DisplayPeriod.valueOf(settings.displayPeriod)
        }.getOrDefault(DisplayPeriod.P2M)
        val currentMeasure = measures.lastOrNull()

        val periodStartMeasure = currentMeasure?.let {
            if (period == DisplayPeriod.ALL) {
                measures.firstOrNull() ?: currentMeasure
            } else {
                val startPeriodDate = with(currentMeasure.date.toLocalDate()) {
                    when (period) {
                        DisplayPeriod.P2W -> minusWeeks(2)
                        DisplayPeriod.P1M -> minusMonths(1)
                        DisplayPeriod.P2M -> minusMonths(2)
                        DisplayPeriod.P3M -> minusMonths(3)
                        DisplayPeriod.P6M -> minusMonths(6)
                        DisplayPeriod.P1Y -> minusYears(1)
                        DisplayPeriod.P2Y -> minusYears(2)
                        DisplayPeriod.P3Y -> minusYears(3)
                    }
                }.plusDays(1)
                Log.d(TAG, "PeriodDate before: $startPeriodDate")
                val startPeriodInstant = startPeriodDate.toInstant()
                measures.lastOrNull {
                    it.date.isBefore(startPeriodInstant)
                } ?: measures.first()
            }
        }
        Log.d(TAG, "Period start measure: $periodStartMeasure")

        val currentWeight = currentMeasure?.convertWeightTo(measureUnit)
        val destWeight = targetWeight?.convertValueTo(measureUnit, measureUnit)
        val periodStartWeight = periodStartMeasure?.convertWeightTo(measureUnit)
        val toTarget =
            if (currentWeight != null && destWeight != null) currentWeight - destWeight else null
        val periodWeightChange =
            if (currentWeight != null && periodStartWeight != null) currentWeight - periodStartWeight else null
        val startIdx = periodStartMeasure?.let { m ->
            measures.indexOf(m)
        } ?: 0

        val chartData = generateWeightChartDataUseCase(
            totalWeightMeasures = measures,
            unit = measureUnit,
            startIdx = startIdx,
            movingAverage1 = settings.ma1,
            movingAverage2 = settings.ma2,
            targetValue = destWeight
        )

        return HomeComputationResult(
            unit = measureUnit,
            period = period,
            movingAverage1 = settings.ma1,
            movingAverage2 = settings.ma2,
            useEmbeddedChart = settings.embeddedChart,
            startWeight = periodStartWeight,
            currentWeight = currentWeight,
            destinationWeight = destWeight,
            periodWeightChange = periodWeightChange,
            toTargetWeight = toTarget,
            chartData = chartData,
        )
    }
}
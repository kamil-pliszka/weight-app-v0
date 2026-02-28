package com.pl.myweightapp.feature.home

import androidx.compose.runtime.Immutable
import com.pl.myweightapp.domain.DisplayPeriod
import com.pl.myweightapp.domain.WeightUnit
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartImage

@Immutable
data class HomeScreenUiState(
    val isLoading: Boolean = false,
    val useEmbeddedChart: Boolean = false,
    val isProcessing: Boolean = false,
    //val chartBitmap: ImageBitmap? = null,
    val chartImage: ChartImage? = null,
    val unit: WeightUnit = WeightUnit.KG,
    val period: DisplayPeriod = DisplayPeriod.P2M,
    val movingAverage1: Int? = null,
    val movingAverage2: Int? = null,
    val startWeight: Float? = null,
    val currentWeight: Float? = null,
    val destinationWeight: Float? = null,
    val periodWeightChange: Float? = null,
    val toTargetWeight: Float? = null,
    val chartWidthPx: Int = 0,
    val chartHeightPx: Int = 0,
    val chartData: ChartData = ChartData(),
)
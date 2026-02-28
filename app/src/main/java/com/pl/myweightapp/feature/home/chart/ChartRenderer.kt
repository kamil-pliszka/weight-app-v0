package com.pl.myweightapp.feature.home.chart

import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartImage

interface ChartRenderer {
    suspend fun render(
        chartData: ChartData,
        widthPx: Int,
        heightPx: Int
    ): ChartImage
}
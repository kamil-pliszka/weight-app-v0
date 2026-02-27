package com.pl.myweightapp.feature.home.chart

import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartImage
import com.pl.myweightapp.domain.chart.ChartLabels

interface ChartRenderer {
    suspend fun render(
        chartData: ChartData,
        chartLabels: ChartLabels,
        widthPx: Int,
        heightPx: Int
    ): ChartImage
}
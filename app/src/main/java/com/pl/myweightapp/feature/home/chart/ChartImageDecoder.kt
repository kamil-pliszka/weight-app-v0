package com.pl.myweightapp.feature.home.chart

import androidx.compose.ui.graphics.ImageBitmap
import com.pl.myweightapp.domain.chart.ChartImage

interface ChartImageDecoder {
    fun decode(chartImage: ChartImage): ImageBitmap
}
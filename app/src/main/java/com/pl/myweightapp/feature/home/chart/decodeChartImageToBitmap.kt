package com.pl.myweightapp.feature.home.chart

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.pl.myweightapp.domain.chart.ChartImage


fun decodeChartImageToBitmap(chartImage: ChartImage): ImageBitmap? {
    return BitmapFactory
        .decodeByteArray(chartImage.bytes, 0, chartImage.bytes.size)
        ?.asImageBitmap()
}
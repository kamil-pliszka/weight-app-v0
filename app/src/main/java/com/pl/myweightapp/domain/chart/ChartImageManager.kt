package com.pl.myweightapp.domain.chart

interface ChartImageManager {
    suspend fun import() : ChartImage?

    suspend fun export(
        image: ChartImage
    )
}
package com.pl.myweightapp.domain.chart

interface ChartImageExporter {
    suspend fun export(
        image: ChartImage
    )
}
package com.pl.myweightapp.domain.chart

interface ChartImageImporter {
    suspend fun import() : ChartImage?
}
package com.pl.myweightapp.domain.chart

data class ChartData(
    val measures: List<ChartMeasure> = listOf(),
    val average: Float? = null,
    val movingAverage1: List<ChartMeasure> = listOf(), //TODO - do zatanowienia czy nie chcemy tutaj listy samych wartości Y (bez czasu)
    val movingAverage2: List<ChartMeasure> = listOf(), //TODO - bo punkty na osi X będą takie same jak w measures
    val mav1Period: Int? = null,
    val mav2Period: Int? = null,
    val targetValue: Float? = null,
    val periodOnChart: Float = 0f, //periodOnChart in Days
)
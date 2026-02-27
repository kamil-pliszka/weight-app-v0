package com.pl.myweightapp.domain

data class AppSettings(
    val language: String,
    val displayPeriod: String,
    val ma1: Int?,
    val ma2: Int?,
    val embeddedChart: Boolean,
)
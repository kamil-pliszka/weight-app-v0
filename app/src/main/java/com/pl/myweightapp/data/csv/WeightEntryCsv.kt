package com.pl.myweightapp.data.csv

import java.math.BigDecimal
import java.time.Instant

data class WeightEntryCsv(
    val timestamp: Instant,
    val value: BigDecimal,
    val unit: WeightUnitCsv
)
package com.pl.myweightapp.domain

import java.math.BigDecimal
import java.time.Instant

data class WeightMeasure(
    val id: Long = 0,
    val date: Instant,
    val weight: BigDecimal,
    val unit: WeightUnit,
    )


fun WeightMeasure.convertWeightTo(dstUnit: WeightUnit): Float {
    return this.weight.convertValueTo(this.unit, dstUnit)
}
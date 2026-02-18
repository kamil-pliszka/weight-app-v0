package com.pl.myweightapp.core.util

import java.math.BigDecimal
import java.math.RoundingMode

fun Float.kgToLbs(): Float {
    return this * 2.20462f
}

fun Float.lbsToKg(): Float {
    return this * 0.453592f
}

fun BigDecimal.toFloat1(): Float {
    return this.setScale(1, RoundingMode.HALF_UP).toFloat()
}
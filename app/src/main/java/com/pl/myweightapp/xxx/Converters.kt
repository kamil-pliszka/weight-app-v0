package com.pl.myweightapp.xxx

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
fun Instant.toDateString(): String {
    return this
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

fun Instant.toLocalDate(): LocalDate {
    return this
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun LocalDate.toDateString(): String {
    return this.format(formatter)
}

fun LocalDate.toInstant(): Instant {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant()
}

fun LocalDate.toMillis(): Long {
    return this.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun Long.millisToLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault()) // strefa urządzenia
        .toLocalDate()
}


fun Float.kgToLbs(): Float {
    return this * 2.20462f
}

fun Float.lbsToKg(): Float {
    return this * 0.453592f
}

fun BigDecimal.toFloat1(): Float {
    return this.setScale(1, RoundingMode.HALF_UP).toFloat()
}

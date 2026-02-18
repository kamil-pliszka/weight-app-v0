package com.pl.myweightapp.core.util

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

fun Instant.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return this
        .atZone(zoneId)
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
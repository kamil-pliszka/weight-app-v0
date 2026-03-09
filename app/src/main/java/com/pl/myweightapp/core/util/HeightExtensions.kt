package com.pl.myweightapp.core.util

private const val CM_PER_INCH = 2.54f

fun Float.cmToInch(): Float {
    return this / CM_PER_INCH
}

fun Float.inchToCm(): Float {
    return this * CM_PER_INCH
}

fun Float.round2() = (this * 100).toInt() / 100f
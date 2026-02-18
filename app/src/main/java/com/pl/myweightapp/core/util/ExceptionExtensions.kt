package com.pl.myweightapp.core.util

fun exceptionToString(e: Throwable) : String {
    return e.localizedMessage ?: (e.javaClass.simpleName + " : " + e.stackTrace.firstOrNull()?.toString())
}
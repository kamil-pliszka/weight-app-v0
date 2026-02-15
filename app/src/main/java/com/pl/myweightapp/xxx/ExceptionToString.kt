package com.pl.myweightapp.xxx

fun exceptionToString(e: Throwable) : String {
    return e.localizedMessage ?: (e.javaClass.simpleName + " : " + e.stackTrace.firstOrNull()?.toString())
}
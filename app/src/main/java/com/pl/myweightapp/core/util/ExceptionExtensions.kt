package com.pl.myweightapp.core.util

fun exceptionToString(e: Throwable) : String {
    return e.localizedMessage ?: e.message ?: (e.javaClass.simpleName + " : " + e.stackTrace.firstOrNull()?.toString())
}
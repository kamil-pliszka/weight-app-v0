package com.pl.myweightapp.domain

interface ResourceProvider {
    fun getString(resId: Int, vararg args: Any): String
}
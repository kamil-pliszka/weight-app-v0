package com.pl.myweightapp.core.domain


import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

    val settingsFlow: Flow<AppSettings>

    val languageFlow: Flow<String>

    suspend fun getLanguageOnce(): String

    suspend fun updateLanguage(language: String)

    suspend fun updatePeriod(period: String)

    suspend fun updateMovingAverages(ma1: Int?, ma2: Int?)

    suspend fun clear()
}
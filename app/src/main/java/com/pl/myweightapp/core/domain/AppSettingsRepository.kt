package com.pl.myweightapp.core.domain


import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

    val settingsFlow: Flow<AppSettings>

    //TODO - to raczej do wywalenia
    val languageFlow: Flow<String>

    suspend fun getLanguageOnce(): String

    suspend fun updateLanguage(language: String)

    suspend fun updatePeriod(period: DisplayPeriod)

    suspend fun updateMovingAverages(ma1: Int?, ma2: Int?)

    suspend fun clear()
}
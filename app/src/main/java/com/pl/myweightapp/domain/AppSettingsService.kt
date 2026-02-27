package com.pl.myweightapp.domain

import kotlinx.coroutines.flow.Flow

interface AppSettingsService {
    val settingsFlow: Flow<AppSettings>
    val languageFlow: Flow<String>

    suspend fun bootstrapLang()
    suspend fun changeLanguage(lang: String)
    suspend fun changePeriod(period: String)
    suspend fun changeMovingAverages(ma1: Int?, ma2: Int?)
    suspend fun updateEmbeddedChart(embeddedChart: Boolean)
    suspend fun deleteAll()
}
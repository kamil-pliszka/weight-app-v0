package com.pl.myweightapp.data.repository

import com.pl.myweightapp.core.domain.AppSettings
import com.pl.myweightapp.core.domain.AppSettingsRepository
import com.pl.myweightapp.data.preferences.AppSettingsDataSource
import kotlinx.coroutines.flow.Flow

class AppSettingsRepositoryImpl(
    private val dataSource: AppSettingsDataSource
) : AppSettingsRepository {
    override val settingsFlow: Flow<AppSettings> = dataSource.settingsFlow
    override val languageFlow: Flow<String> = dataSource.languageFlow

    override suspend fun getLanguageOnce(): String = dataSource.getLanguageOnce()

    override suspend fun updateLanguage(language: String) {
        dataSource.updateLanguage(language)
    }

    override suspend fun updatePeriod(period: String) {
        dataSource.updatePeriod(period)
    }

    override suspend fun updateMovingAverages(ma1: Int?, ma2: Int?) {
        dataSource.updateMovingAverages(ma1, ma2)
    }

    override suspend fun updateEmbeddedChart(embeddedChart: Boolean) {
        dataSource.updateEmbeddedChart(embeddedChart)
    }

    override suspend fun clear() {
        dataSource.deleteAll()
    }

}
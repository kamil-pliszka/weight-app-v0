package com.pl.myweightapp

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.pl.myweightapp.repositories.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class AppSettingsManager(
    private val repository: AppSettingsRepository
) {
    companion object {
        private const val TAG = "AppSettingsManager"
    }

    val languageFlow: Flow<String> =
        repository.languageFlow.distinctUntilChanged()
    val settingsFlow = repository.settingsFlow

    suspend fun bootstrapLang() {
        val lang = repository.getLanguageOnce()
        Log.d(TAG,"bootstrap initializeLang : $lang")
        applyLocale(lang)
    }

    suspend fun changeLanguage(lang: String) {
        applyLocale(lang)
        repository.updateLanguage(lang)
    }

    private fun applyLocale(lang: String) {
        Log.d(TAG, "applyLocale: $lang")
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(lang)
        )
    }

    suspend fun changePeriod(period: String) {
        repository.updatePeriod(period)
    }

    suspend fun changeMovingAverages(ma1: Int?, ma2: Int?) {
        repository.updateMovingAverages(ma1, ma2)
    }

    suspend fun deleteAll() {
        repository.deleteAll()
    }
}

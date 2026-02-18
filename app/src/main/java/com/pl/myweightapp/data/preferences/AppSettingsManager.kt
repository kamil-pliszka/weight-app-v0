package com.pl.myweightapp.data.preferences

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.pl.myweightapp.core.domain.AppSettings
import com.pl.myweightapp.core.domain.DisplayPeriod
import com.pl.myweightapp.data.repository.AppSettingsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/**
 * Manager do wygodnego dostępu do ustawień aplikacji w całej aplikacji.
 * Oparty o AppSettingsRepositoryImpl.
 */
class AppSettingsManager(
    private val repository: AppSettingsRepositoryImpl,
    scope: CoroutineScope
) {
    companion object {
        private const val TAG = "AppSettingsManager"
    }

    //TODO - raczej do usuniecia
    val languageFlow: Flow<String> =
        repository.languageFlow.distinctUntilChanged()
    //val settingsFlow = repository.settingsFlow
    // StateFlow dla całej aplikacji – zawsze aktualne ustawienia
    val settingsFlow: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings(
                language = "en",
                displayPeriod = DisplayPeriod.P3M.name,
                ma1 = null,
                ma2 = null
            )
        )


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

    suspend fun changePeriod(period: DisplayPeriod) {
        repository.updatePeriod(period)
    }

    suspend fun changeMovingAverages(ma1: Int?, ma2: Int?) {
        repository.updateMovingAverages(ma1, ma2)
    }

    suspend fun deleteAll() {
        repository.clear()
    }
}
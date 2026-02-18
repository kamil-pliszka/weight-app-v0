package com.pl.myweightapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.core.domain.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

val Context.dataStore by preferencesDataStore(name = "settings")

class AppSettingsDataSource(
    private val context: Context
) {
    companion object {
        private val LANG_KEY = stringPreferencesKey("app_language")
        private val PERIOD_KEY = stringPreferencesKey("display_period")
        private val MA1_KEY = intPreferencesKey("ma1")
        private val MA2_KEY = intPreferencesKey("ma2")
    }

    val languageFlow: Flow<String> =
        context.dataStore.data
            .map { it[LANG_KEY] ?: Locale.getDefault().toLanguageTag() }
            .distinctUntilChanged()

    suspend fun updateLanguage(lang: String) = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            prefs[LANG_KEY] = lang
        }
    }

    suspend fun updatePeriod(period: String) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[PERIOD_KEY] = period }
    }

    suspend fun updateMovingAverages(ma1: Int?, ma2: Int?) = withContext(Dispatchers.IO) {
        context.dataStore.edit {
            if (ma1 == null) it.remove(MA1_KEY)
            else it[MA1_KEY] = ma1
            if (ma2 == null) it.remove(MA2_KEY)
            else it[MA2_KEY] = ma2
        }
    }

    suspend fun getLanguageOnce(): String = languageFlow.first()


    val settingsFlow: Flow<AppSettings> =
        context.dataStore.data
            .map { prefs ->
                AppSettings(
                    language = prefs[LANG_KEY] ?: Locale.getDefault().toLanguageTag(),
                    displayPeriod = prefs[PERIOD_KEY] ?: Constants.DEFAULT_DISPLAY_PERIOD,
                    ma1 = prefs[MA1_KEY],
                    ma2 = prefs[MA2_KEY]
                )
            }
            .distinctUntilChanged()

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            prefs.clear() // usuwa wszystkie klucze zapisane w DataStore
        }
    }
}

package com.pl.myweightapp.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pl.myweightapp.core.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class LanguageRepository(private val context: Context) {
    private val LANG_KEY = stringPreferencesKey("app_language")

    val languageFlow: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[LANG_KEY] ?: Constants.DEFAULT_LANG
        }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[LANG_KEY] = lang
        }
    }

    suspend fun getLanguageOnce(): String =
        languageFlow.first()
}
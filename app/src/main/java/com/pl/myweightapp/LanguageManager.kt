package com.pl.myweightapp

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.pl.myweightapp.repositories.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "LanguageManager"
class LanguageManager(
    private val repository: LanguageRepository
) {

    val languageFlow: Flow<String> =
        repository.languageFlow.distinctUntilChanged()

    suspend fun bootstrap() {
        val lang = repository.getLanguageOnce()
        Log.d(TAG,"bootstrap initializeLang : $lang")
        applyLocale(lang)
    }

    suspend fun changeLanguage(lang: String) {
        applyLocale(lang)
        repository.setLanguage(lang)
    }

    private fun applyLocale(lang: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(lang)
        )
    }
}

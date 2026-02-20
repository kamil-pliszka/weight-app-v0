package com.pl.myweightapp.feature.settings

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.app.di.AppModule
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.core.presentation.DefaultUiEventOwner
import com.pl.myweightapp.core.presentation.UiEventOwner
import com.pl.myweightapp.core.presentation.launchSafely
import com.pl.myweightapp.core.presentation.sendInfo
import com.pl.myweightapp.data.csv.CsvParseException
import com.pl.myweightapp.data.csv.exportWeightCsv
import com.pl.myweightapp.data.csv.getFileNameFromUri
import com.pl.myweightapp.data.csv.importWeightCsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Immutable
data class UiState(
    val isLoading: Boolean = false,
    val isCsvProcessing: Boolean = false,
    val csvProgress: Float = 0f, // 0..1
    val showDeleteConfirm: Boolean = false,
    val showLanguageChooser: Boolean = false,
    val langTag: String = "",
    val langDisplayResId: Int? = null,
    val useEmbeddedChart: Boolean = false,
)

sealed interface Action {
    data class OnCsvImport(val uri: Uri) : Action
    data class OnCsvExport(val uri: Uri) : Action
    object OnDeleteAllDataClick : Action      // klik przycisku
    object OnDeleteAllDataConfirm : Action    // potwierdzenie
    object OnDeleteAllDataCancel : Action     // anulowanie
    object OnLanguageClick : Action
    object OnLanguageDismiss : Action
    data class OnLanguageChoose(val lang: String) : Action
    data class OnChangeUseEmbeddedChart(val embedded: Boolean) : Action
}

class SettingsViewModel : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private const val TAG = "SettingsVM"
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private val appSettingsManager = AppModule.provideAppSettingsManager()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsManager.settingsFlow.collect { settings ->
                Log.d(TAG, "observeSettings: $settings")
                _state.update {
                    it.copy(
                        langTag = settings.language,
                        langDisplayResId = langDisplayResId(settings.language),
                        useEmbeddedChart = settings.embeddedChart
                    )
                }
            }
        }
    }

    private fun langDisplayResId(tag: String): Int? {
        return when (tag) {
            "pl" -> R.string.lang_pl
            "en" -> R.string.lang_en
            else -> null
        }
    }


    fun onAction(action: Action) {
        when (action) {
            Action.OnDeleteAllDataClick -> {
                _state.update { it.copy(showDeleteConfirm = true) }
            }

            Action.OnDeleteAllDataCancel -> {
                _state.update { it.copy(showDeleteConfirm = false) }
            }

            Action.OnDeleteAllDataConfirm -> {
                _state.update { it.copy(showDeleteConfirm = false) }
                processDeleteAllData()
            }

            is Action.OnCsvImport -> {
                //_state.update { it.copy(showImportCsvPicker = false) }
                //uri jest null gdy nie wybrano pliku, lub użyto przycisku back
                processCsvImport(action.uri)
            }

            is Action.OnCsvExport -> {
                processCsvExport(action.uri)
            }

            Action.OnLanguageClick -> {
                _state.update { it.copy(showLanguageChooser = true) }
            }

            Action.OnLanguageDismiss -> {
                _state.update { it.copy(showLanguageChooser = false) }
            }

            is Action.OnLanguageChoose -> {
                setLanguage(action.lang)
                _state.update { it.copy(showLanguageChooser = false) }
            }

            is Action.OnChangeUseEmbeddedChart -> {
                changeUseEmbeddedChart(action.embedded)
            }
        }
    }

    private suspend inline fun <T> withLoading(
        crossinline block: suspend () -> T
    ): T {
        return try {
            _state.update { it.copy(isLoading = true) }
            block()
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend inline fun <T> withCsvProcessing(
        crossinline block: suspend () -> T
    ): T {
        return try {
            _state.update { it.copy(isCsvProcessing = true) }
            block()
        } finally {
            _state.update { it.copy(isCsvProcessing = false) }
        }
    }

    private fun processCsvImport(uri: Uri) {
        Log.d(TAG, "CSV import uri: $uri")
        launchSafely {
            withCsvProcessing {
                val context = AppModule.provideContext()
                val mime = context.contentResolver.getType(uri)
                Log.d(TAG, "mime: $mime")
                try {
                    val entriesCount = importWeightCsv(context, uri) { progress ->
                        _state.update { it.copy(csvProgress = progress) }
                    }
                    sendInfo(R.string.settings_csv_import_success, entriesCount)
                } catch (e: CsvParseException) {
                    sendInfo(R.string.settings_csv_import_parsing_error, e.message ?: "")
                }
            }
        }
    }

    fun suggestedExportFileName(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val dateStr = LocalDateTime.now().format(formatter)
        return "MyWeight_$dateStr.csv"
    }

    private fun processCsvExport(uri: Uri) {
        Log.d(TAG, "CSV export uri: $uri")
        launchSafely {
            withCsvProcessing {
                val context = AppModule.provideContext()
                val mime = context.contentResolver.getType(uri)
                val filename = getFileNameFromUri(context, uri)
                Log.d(TAG, "mime: $mime, filename: $filename")
                val entriesCount = exportWeightCsv(context, uri) { progress ->
                    _state.update { it.copy(csvProgress = progress) }
                }
                sendInfo(
                    R.string.settings_csv_export_success,
                    entriesCount,
                    filename ?: ""
                )
            }
        }
    }

    private fun processDeleteAllData() {
        launchSafely {
            withLoading {
                //usuniecie pomiarów
                AppModule.provideWeightMeasureRepository().deleteAll()
                //usunięcie profilu
                AppModule.provideUserProfileRepository().deleteAll()
                //usunięcie ustawień
                AppModule.provideAppSettingsManager().deleteAll()
                deleteAppFiles()
                sendInfo(R.string.settings_delete_all_success)
            }
        }
    }

    private suspend fun deleteAppFiles() = withContext(Dispatchers.IO) {
        //usunięcie wygenerowanego wykresu
        val context = AppModule.provideContext()
        val fileChart = File(context.filesDir, Constants.WEIGHT_CHART_FILENAME)
        if (fileChart.exists()) {
            Log.d(TAG, "Delete ${Constants.WEIGHT_CHART_FILENAME}")
            fileChart.delete()
        }
        val fileProfile = File(context.filesDir, Constants.PROFILE_PHOTO_FILENAME)
        if (fileProfile.exists()) {
            Log.d(TAG, "Delete ${Constants.PROFILE_PHOTO_FILENAME}")
            fileProfile.delete()
        }
    }

    private fun setLanguage(tag: String) {
        Log.d(TAG, "setLanguage to: $tag")
        launchSafely {
            appSettingsManager.changeLanguage(tag)
        }
    }


    private fun changeUseEmbeddedChart(embeddedChart: Boolean) {
        Log.d(TAG, "changeUseEmbeddeChart to: $embeddedChart")
        launchSafely {
            appSettingsManager.updateEmbeddedChart(embeddedChart)
        }
    }
}


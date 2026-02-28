package com.pl.myweightapp.feature.settings

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.BuildConfig
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.AppSettingsService
import com.pl.myweightapp.domain.BackupService
import com.pl.myweightapp.domain.csv.CsvParseException
import com.pl.myweightapp.domain.csv.CsvService
import com.pl.myweightapp.feature.common.DefaultUiEventOwner
import com.pl.myweightapp.feature.common.UiEventOwner
import com.pl.myweightapp.feature.common.launchSafely
import com.pl.myweightapp.feature.common.sendInfo
import com.pl.myweightapp.feature.common.sendMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject

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
    val visibleRestore: Boolean = false,
    val appVersionDate: String = "",
    val appVersionHash: String = "",
)

sealed interface Action {
    data class OnCsvImport(val input: InputStream) : Action
    data class OnCsvExport(val output: OutputStream, val filename: String?) : Action
    object OnDeleteAllDataClick : Action      // klik przycisku
    object OnDeleteAllDataConfirm : Action    // potwierdzenie
    object OnDeleteAllDataCancel : Action     // anulowanie
    object OnLanguageClick : Action
    object OnLanguageDismiss : Action
    data class OnLanguageChoose(val lang: String) : Action
    data class OnChangeUseEmbeddedChart(val embedded: Boolean) : Action
    object OnTryToRestore : Action
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsService: AppSettingsService,
    private val backupService: BackupService,
    private val csvService: CsvService,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    private val _state = MutableStateFlow(prepareInitialUiState())
    val state = _state.asStateFlow()

    private fun prepareInitialUiState(): UiState {
        val versionHash = BuildConfig.VERSION_NAME
        val instant = java.time.Instant.ofEpochSecond(BuildConfig.VERSION_CODE.toLong())
        val dateTime = instant.atZone(java.time.ZoneId.systemDefault())
        val versionDateStr = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").format(dateTime)
        return UiState(
            appVersionDate = versionDateStr,
            appVersionHash = versionHash.substring(0, 3)
        )
    }


    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsService.settingsFlow.collect { settings ->
                Log.d(TAG, "observeSettings: $settings")
                _state.update {
                    it.copy(
                        langTag = settings.language,
                        langDisplayResId = langDisplayResId(settings.language),
                        useEmbeddedChart = settings.embeddedChart,
                    )
                }
                updateAvailableRestore()
            }
        }
    }

    private fun updateAvailableRestore() {
        viewModelScope.launch {
            val availableRestore = backupService.isAvailableRestore()
            Log.d(TAG, "isAvailableRestore = $availableRestore")
            _state.update {
                it.copy(
                    visibleRestore = availableRestore,
                )
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
                processCsvImport(action.input)
            }

            is Action.OnCsvExport -> {
                processCsvExport(action.output, action.filename)
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

            Action.OnTryToRestore -> tryToRestore()
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

    private fun processCsvImport(input: InputStream) {
        Log.d(TAG, "CSV import")
        launchSafely {
            withCsvProcessing {
                try {
                    val entriesCount =
                        csvService.importWeightCsv(input) { progress ->
                            _state.update { it.copy(csvProgress = progress) }
                        }
                    sendInfo(R.string.settings_csv_import_success, entriesCount)
                } catch (e: CsvParseException) {
                    sendInfo(R.string.settings_csv_import_parsing_error, e.message ?: "")
                }
            }
        }
    }

    private fun processCsvExport(output: OutputStream, filename: String?) {
        Log.d(TAG, "CSV export")
        launchSafely {
            withCsvProcessing {
                val entriesCount =
                    csvService.exportWeightCsv(null, output ) { progress ->
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
                backupService.deleteAll()
                sendInfo(R.string.settings_delete_all_success)
                updateAvailableRestore()
            }
        }
    }


    private fun setLanguage(tag: String) {
        Log.d(TAG, "setLanguage to: $tag")
        launchSafely {
            appSettingsService.changeLanguage(tag)
        }
    }


    private fun changeUseEmbeddedChart(embeddedChart: Boolean) {
        Log.d(TAG, "changeUseEmbeddeChart to: $embeddedChart")
        launchSafely {
            appSettingsService.updateEmbeddedChart(embeddedChart)
        }
    }


    private fun tryToRestore() {
        Log.d(TAG, "tryToRestore")
        launchSafely {
            val msg = backupService.tryToRestoreBackup()
            sendMessage(msg)
            updateAvailableRestore()
        }
    }

}


package com.pl.myweightapp.xxx.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.AppModule
import com.pl.myweightapp.R
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.xxx.csv.CsvParseException
import com.pl.myweightapp.xxx.csv.exportWeightCsv
import com.pl.myweightapp.xxx.csv.getFileNameFromUri
import com.pl.myweightapp.xxx.csv.importWeightCsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

@Immutable
data class UiState(
    val isLoading: Boolean = false,
    val isCsvProcessing: Boolean = false,
    val csvProgress: Float = 0f, // 0..1
    val showDeleteConfirm: Boolean = false,
    val showLanguageChooser: Boolean = false,
    val langTag: String = "",
    val langDisplayResId: Int? = null,
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
}

sealed interface UiEvent {
    //data object OpenFilePicker : UiEvent
    data class Error(val message: String) : UiEvent
    data class Info(val message: String) : UiEvent
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "SettingsVM"
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val languageManager = AppModule.provideAppSettingsManager()

    init {
        observeLanguage()
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            languageManager.languageFlow.collect { tag ->
                Log.d(TAG,"observeLanguage: $tag")
                _state.update {
                    it.copy(
                        langTag = tag,
                        langDisplayResId = langDisplayResId(tag)
                    )
                }
            }
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
                Log.d(TAG, "lang: ${action.lang}")
                setLanguage(action.lang)
                _state.update { it.copy(showLanguageChooser = false) }
            }
        }
    }

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    private fun processCsvImport(uri: Uri) {
        Log.d(TAG, "CSV import uri: $uri")
        val context = (getApplication() as Context)
        val mime = context.contentResolver.getType(uri)
        Log.d(TAG, "mime: $mime")
        viewModelScope.launch {
            _state.update { it.copy(isCsvProcessing = true) }
            try {
                val entriesCount = importWeightCsv(context, uri) { progress ->
                    _state.update { it.copy(csvProgress = progress) }
                }
                sendEvent(UiEvent.Info(
                    context.getString(
                        R.string.settings_csv_import_success,
                        entriesCount
                    )))
            } catch (e: CancellationException) {
                throw e
            } catch (e: CsvParseException) {
                sendEvent(UiEvent.Error(
                    context.getString(
                        R.string.settings_csv_import_parsing_error,
                        e.message
                    )))
            } catch (e: Exception) {
                Log.e("CsvImport", e.message, e)
                sendEvent(UiEvent.Error(
                    context.getString(
                        R.string.settings_csv_import_error,
                        e.message
                    )))
            } finally {
                _state.update { it.copy(isCsvProcessing = false) }
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
        val context = (getApplication() as Context)
        val mime = context.contentResolver.getType(uri)
        val filename = getFileNameFromUri(context, uri)
        Log.d(TAG, "mime: $mime, filename: $filename")
        viewModelScope.launch {
            _state.update { it.copy(isCsvProcessing = true) }
            try {
                val entriesCount = exportWeightCsv(context, uri) { progress ->
                    _state.update { it.copy(csvProgress = progress) }
                }
                sendEvent(UiEvent.Info(
                    context.getString(
                        R.string.settings_csv_export_success,
                        entriesCount,
                        filename
                    )))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CsvExport", e.message, e)
                sendEvent(UiEvent.Error(
                    context.getString(
                        R.string.settings_csv_export_error,
                        e.message
                    )))
            } finally {
                _state.update { it.copy(isCsvProcessing = false) }
            }
        }
    }

    private fun processDeleteAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                //usuniecie pomiarów
                AppModule.provideWeightMeasureRepository().deleteAll()
                //usunięcie profilu
                AppModule.provideUserProfileRepository().deleteAll()
                //usunięcie ustawień
                AppModule.provideAppSettingsManager().deleteAll()
                deleteAppFiles()
                sendEvent(UiEvent.Info((getApplication() as Context).getString(R.string.settings_delete_all_success)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("DeleteAll", e.message, e)
                sendEvent(UiEvent.Error(
                    (getApplication() as Context).getString(
                        R.string.settings_delete_all_error,
                        e.message
                    )))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun deleteAppFiles() = withContext(Dispatchers.IO) {
        //usunięcie wygenerowanego wykresu
        val context = (getApplication() as Context)
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

    fun setLanguage(tag: String) {
        Log.d(TAG, "setLanguage to: $tag")
//        AppCompatDelegate.setApplicationLocales(
//            LocaleListCompat.forLanguageTags(tag)
//        )
        viewModelScope.launch {
            try {
                //AppModule.provideUserProfileRepository().updateLang(tag)
                //loadInitialLanguage()
                //viewModelScope.launch {
                languageManager.changeLanguage(tag)
                //}
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("setLanguage", e.message, e)
                sendEvent(UiEvent.Error((getApplication() as Context).getString(R.string.settings_lang_error, e.message)))
            }
        }
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val context = (getApplication() as Context)
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(tag)
        }*/
    }

    private fun langDisplayResId(tag: String): Int? {
        return when (tag) {
            "pl" -> R.string.lang_pl
            "en" -> R.string.lang_en
            else -> null
        }
    }
}


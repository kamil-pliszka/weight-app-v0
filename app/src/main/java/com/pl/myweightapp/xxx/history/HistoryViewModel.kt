package com.pl.myweightapp.xxx.history

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.AppModule
import com.pl.myweightapp.persistence.WeightMeasureEntity
import com.pl.myweightapp.repositories.sortWeightMeasureHistory
import com.pl.myweightapp.xxx.WieghtMeasureUi
import com.pl.myweightapp.xxx.toWeightMeasureUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface HistoryAction {
    data class OnItemEditAction(val itemUI: WieghtMeasureUi) : HistoryAction
    object OnCloseEditAction : HistoryAction
    data class OnItemDeleteAction(val itemUI: WieghtMeasureUi) : HistoryAction
    data class OnConfirmDeleteAction(val itemId: Long) : HistoryAction
    object OnCancelDeleteAction : HistoryAction
    object OnRefreshAction : HistoryAction
}

sealed interface UiEvent {
    data object Deleted : UiEvent
    data class Error(val message: String) : UiEvent
}


@Immutable
data class HistoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val measurements: List<WieghtMeasureUi> = emptyList(),
    val editingItemId: Long? = null,
    val deletingItem: WieghtMeasureUi? = null,
)

private const val TAG = "HistoryVM"
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel : ViewModel() {
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state

    private val repository = AppModule.provideWeightMeasureRepository()
    private val refreshTrigger = MutableSharedFlow<Unit>()

    init {
        refreshTrigger
            .onStart {
                // pierwszy start ViewModelu
                _state.update { it.copy(isLoading = true) }
                emit(Unit)
            }
            .flatMapLatest {
                Log.d(TAG,"Refresh trigger started")
                repository.observeWeightMeasureHistory()
            }
            .map { convertToHistoryUi(it) }
            .onEach { uiList ->
                Log.d(TAG,"Got results from DB, size: ${uiList.size}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        measurements = uiList
                    )
                }
            }
            .launchIn(viewModelScope)
    }

//    init {
//        viewModelScope.launch {
//            AppModule.provideWeightMeasureRepository()
//                .observeWeightMeasureHistory()
//                .collect { history ->
//                    val uiList = convertToHistoryUi(history)
//                    _state.update { current ->
//                        current.copy(
//                            isLoading = false,
//                            measurements = uiList
//                        )
//                    }
//                }
//        }
//    }

//    private fun launchWithErrorHandling(
//        block: suspend () -> Unit
//    ) {
//        viewModelScope.launch {
//            try {
//                block()
//            } catch (e: CancellationException) {
//                throw e
//            } catch (e: Throwable) {
//                Log.e("HistoryViewModel", "error", e)
//                //withContext(Dispatchers.Main) {
//                _events.send(ModelEvent.IOError(exceptionToString(e)))
//                //}
//            }
//        }
//    }
//
//    private fun loadHistory() {
//        launchWithErrorHandling {
//            _state.update { it.copy(isLoading = true) }
//            val weightMeasureHistory = withContext(Dispatchers.IO) {
//                MyContainer.provideWeightMeasureRepository().findWeightMeasureHistory()
//            }
//            _state.update {
//                it.copy(
//                    isLoading = false,
//                    measurements = convertToHistoryUi(weightMeasureHistory)
//                )
//            }
//        }
//    }

    private fun convertToHistoryUi(history: List<WeightMeasureEntity>): List<WieghtMeasureUi> {
        val historySorted = sortWeightMeasureHistory(history)
        return historySorted.mapIndexed { idx, elem ->
            val prevWeight = historySorted.getOrNull(idx + 1)?.weight
            val change = prevWeight?.let { elem.weight - it }
            elem.toWeightMeasureUi(change)
        }
    }


    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.OnItemEditAction -> {
                _state.update { it.copy(editingItemId = action.itemUI.id) }
            }

            HistoryAction.OnCloseEditAction -> {
                _state.update { it.copy(editingItemId = null) }
            }

            is HistoryAction.OnItemDeleteAction -> {
                _state.update { it.copy(deletingItem = action.itemUI) }
            }

            is HistoryAction.OnConfirmDeleteAction -> {
                onConfirmDelete(action.itemId)
            }

            HistoryAction.OnCancelDeleteAction -> {
                _state.update { it.copy(deletingItem = null) }
            }

            HistoryAction.OnRefreshAction -> {
                Log.d(TAG,"OnRefreshAction")
                _state.update { it.copy(isRefreshing = true) }
                viewModelScope.launch {
                    refreshTrigger.emit(Unit)
                }
            }
        }
    }

    private fun onConfirmDelete(itemId: Long) {
        viewModelScope.launch {
            try {
                repository.delete(itemId)
                _events.send(UiEvent.Deleted)
                _state.update { it.copy(deletingItem = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(UiEvent.Error("Delete failed: ${e.message}"))
            }
        }
    }

}


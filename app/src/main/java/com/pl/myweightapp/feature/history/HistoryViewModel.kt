package com.pl.myweightapp.feature.history

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.feature.common.DefaultUiEventOwner
import com.pl.myweightapp.feature.common.UiEventOwner
import com.pl.myweightapp.feature.common.launchWithErrorHandling
import com.pl.myweightapp.feature.common.sendInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryAction {
    data class OnItemEditAction(val itemUI: WeightMeasureUi) : HistoryAction
    data class OnItemDeleteAction(val itemUI: WeightMeasureUi) : HistoryAction
    data class OnConfirmDeleteAction(val itemId: Long) : HistoryAction
    object OnCancelDeleteAction : HistoryAction
    object OnRefreshAction : HistoryAction
}

@Immutable
data class HistoryUiState(
    //val isLoading: Boolean = false,
    //val isRefreshing: Boolean = false,
    //val measurements: List<WeightMeasureUi> = emptyList(),
    val deletingItem: WeightMeasureUi? = null,
)

sealed interface HistoryUiEvent {
    data class NavToEditMeasure(val id: Long) : HistoryUiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    val repository: WeightMeasureRepository,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state

    private val _navEvents = MutableSharedFlow<HistoryUiEvent>(
        extraBufferCapacity = 1
    )
    val navEvents = _navEvents.asSharedFlow()

    private suspend fun sendNavToEditMeasure(id: Long) {
        _navEvents.emit(HistoryUiEvent.NavToEditMeasure(id))
    }

    // w zasadzie niepotrzebne, room sam odświeża dane po ich modyfikacji
    // trigger dodany żeby przetestować swipeToRefresh
    // pozostawiony ze względu na przyszłe:
    // możliwość że dojdzie synchronizacja z backendem
    // architektura gotową na remote source
//    private val refreshTrigger = MutableSharedFlow<Unit>(
//        extraBufferCapacity = 1
//    )

    val measurements = repository.getPagedHistory()
        .map { pagingData ->
            pagingData.map { (measure, change) ->
                measure.toWeightMeasureUi(change)
            }
        }
        .cachedIn(viewModelScope)

//    init {
//        refreshTrigger
//            .onStart {
//                emit(Unit)
//                _state.update { it.copy(isLoading = true) }
//            }
//            .flatMapLatest { repository.observeWeightMeasureHistory() }
//            .map { convertToHistoryUi(it) }
//            .onEach { uiList ->
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        isRefreshing = false,
//                        measurements = uiList
//                    )
//                }
//            }
//            .catch { e ->
//                Log.e(TAG, "Error loading history", e)
//                sendException(e)
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        isRefreshing = false
//                    )
//                }
//            }
//            .launchIn(viewModelScope)
//
//    }

    /**
     * historySorted must be sorted DESC by date
     */
//    private fun convertToHistoryUi(historySorted: List<WeightMeasure>): List<WeightMeasureUi> {
//        return historySorted.mapIndexed { idx, elem ->
//            val prevWeight = historySorted.getOrNull(idx + 1)?.weight
//            val change = prevWeight?.let { elem.weight - it }
//            elem.toWeightMeasureUi(change)
//        }
//    }


    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.OnItemEditAction -> {
                viewModelScope.launch {
                    sendNavToEditMeasure(action.itemUI.id)
                }
            }

            /*HistoryAction.OnCloseEditAction -> {
                _state.update { it.copy(editingItemId = null) }
            }*/

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
                Log.d(TAG, "OnRefreshAction")
                //_state.update { it.copy(isRefreshing = true) }
                //refreshTrigger.tryEmit(Unit)
            }
        }
    }

    private fun onConfirmDelete(itemId: Long) {
        launchWithErrorHandling {
            //Room sam wyemituje nową listę po delete, nie trzeba recznie odświeżać
            repository.delete(itemId)
            sendInfo(R.string.successfully_deleted)
            _state.update { it.copy(deletingItem = null) }
        }
    }

}


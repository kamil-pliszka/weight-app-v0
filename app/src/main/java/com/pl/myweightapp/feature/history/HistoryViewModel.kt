package com.pl.myweightapp.feature.history

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.pl.myweightapp.R
import com.pl.myweightapp.core.presentation.DefaultUiEventOwner
import com.pl.myweightapp.core.presentation.UiEventOwner
import com.pl.myweightapp.core.presentation.launchSafely
import com.pl.myweightapp.core.presentation.sendInfo
import com.pl.myweightapp.data.local.WeightMeasureEntity
import com.pl.myweightapp.data.repository.WeightMeasureRepository
import com.pl.myweightapp.data.repository.sortWeightMeasureHistory
import com.pl.myweightapp.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryAction {
    data class OnItemEditAction(val itemUI: WieghtMeasureUi) : HistoryAction
    object OnCloseEditAction : HistoryAction
    data class OnItemDeleteAction(val itemUI: WieghtMeasureUi) : HistoryAction
    data class OnConfirmDeleteAction(val itemId: Long) : HistoryAction
    object OnCancelDeleteAction : HistoryAction
    object OnRefreshAction : HistoryAction
}

@Immutable
data class HistoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val measurements: List<WieghtMeasureUi> = emptyList(),
    val editingItemId: Long? = null,
    val deletingItem: WieghtMeasureUi? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
//class HistoryViewModel : ViewModel() {
@HiltViewModel
class HistoryViewModel @Inject constructor(
    val repository: WeightMeasureRepository,
): ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private const val TAG = "HistoryVM"
    }

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state

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


    fun onAction(action: HistoryAction, navController: NavController) {
        when (action) {
            is HistoryAction.OnItemEditAction -> {
                //_state.update { it.copy(editingItemId = action.itemUI.id) }
                navController.navigate("${Screen.Edit.route}/${action.itemUI.id}")
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
        launchSafely {
            repository.delete(itemId)
            sendInfo(R.string.successfully_deleted)
            _state.update { it.copy(deletingItem = null) }
        }
    }

}


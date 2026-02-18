package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.app.di.AppModule
import com.pl.myweightapp.feature.history.WeightUnitUi
import com.pl.myweightapp.feature.history.toWeightUnit
import com.pl.myweightapp.feature.history.toWeightUnitUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

//@Immutable
sealed interface EditMeasureUiState {
    data object Loading : EditMeasureUiState
    data class Loaded(
        val date: Instant,
        val weight: BigDecimal,
        val unit: WeightUnitUi,
        val showDeleteConfirm: Boolean = false
    ) : EditMeasureUiState

    data object Saving : EditMeasureUiState

    //data object Saved : EditMeasureUiState
    data object Deleted : EditMeasureUiState
    //data class Error(val message: String) : EditMeasureUiState
}

sealed interface UiEvent {
    data object Saved : UiEvent
    data object Deleted : UiEvent
    data class Error(val message: String) : UiEvent
}

class EditMeasureViewModel(
    val itemId: Long,
) : ViewModel() {
    companion object {
        private const val TAG = "EditMeasureVM"
    }

    //output events
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _state = MutableStateFlow<EditMeasureUiState>(EditMeasureUiState.Loading)
    val state = _state.asStateFlow()

    init {
        observeItem()
    }

    private fun observeItem() {
        viewModelScope.launch {
            AppModule.provideWeightMeasureRepository()
                .observeById(itemId)
                .collect { entity ->
                    Log.d(TAG,"Got refreshed entity: $itemId")
                    if (entity == null) {
                        _state.value = EditMeasureUiState.Deleted
                    } else {
                        _state.value = EditMeasureUiState.Loaded(
                            date = entity.date,
                            weight = entity.weight,
                            unit = entity.unit.toWeightUnitUi()
                        )
                    }
                }
        }
    }


    fun updateMeasure(weight: BigDecimal) {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(
                weight = weight
            )
        }
    }

    fun updateWeightUnit(weightUnit: WeightUnitUi) {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(
                unit = weightUnit
            )
        }
    }


    fun onSaveAction(/*closeDialogHandler : () -> Unit*/) {
        val current = _state.value
        if (current !is EditMeasureUiState.Loaded) return

        _state.value = EditMeasureUiState.Saving
        viewModelScope.launch {
            try {
                AppModule.provideWeightMeasureRepository().update(
                    id = itemId,
                    date = current.date,
                    weight = current.weight,
                    unit = current.unit.toWeightUnit()
                )
                //_state.value = EditMeasureUiState.Saved
                //_state.value = current
                _events.send(UiEvent.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                //_state.value = EditMeasureUiState.Error(e.message ?: "Save failed")
                _events.send(UiEvent.Error("Save failed: ${e.message}"))
            }
        }
    }

    fun onDeleteAction() {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(showDeleteConfirm = true)
        }
    }

    fun onCancelDelete() {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy( showDeleteConfirm = false)
        }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            try {
                AppModule.provideWeightMeasureRepository().delete(itemId)
                _events.send(UiEvent.Deleted)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(UiEvent.Error("Delete failed: ${e.message}"))
            }
        }
    }

}
package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.feature.common.DefaultUiEventOwner
import com.pl.myweightapp.feature.common.UiEventOwner
import com.pl.myweightapp.feature.common.launchWithErrorHandling
import com.pl.myweightapp.feature.common.sendInfo
import com.pl.myweightapp.feature.common.ui.WeightUnitUi
import com.pl.myweightapp.feature.common.ui.toWeightUnit
import com.pl.myweightapp.feature.common.ui.toWeightUnitUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject

//@Immutable
sealed interface EditMeasureUiState {
    data object Loading : EditMeasureUiState
    data class Loaded(
        val date: Instant,
        val weight: BigDecimal,
        val unit: WeightUnitUi,
        val showDeleteConfirm: Boolean = false
    ) : EditMeasureUiState

    data object Processing : EditMeasureUiState
}

sealed interface EditAction {
    object OnDeleteAction: EditAction
    object OnSaveAction: EditAction
    data class OnUpdateMeasure(val newMeasure: BigDecimal): EditAction
    object OnConfirmDelete: EditAction
    object OnCancelDelete: EditAction
    object OnDismissAction: EditAction
    object ToggleWeightUnit: EditAction
}

@HiltViewModel
class EditMeasureViewModel @Inject constructor(
    //private val itemId: Long,
    private val repository: WeightMeasureRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    private val itemId: Long = savedStateHandle["itemId"] ?: error("itemId is required")

    private val _navEvents = MutableSharedFlow<AddEditMeasureEvent>(
        extraBufferCapacity = 1
    )
    val navEvents = _navEvents.asSharedFlow()

    private suspend fun sendCloseDialogEvent() {
        _navEvents.emit(AddEditMeasureEvent.CloseDialog)
    }

    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    private val _state = MutableStateFlow<EditMeasureUiState>(EditMeasureUiState.Loading)
    val state = _state.asStateFlow()

    init {
        observeItem()
    }

    private fun observeItem() {
        viewModelScope.launch {
            repository
                .observeById(itemId)
                .collect { entity ->
                    Log.d(TAG, "Got refreshed entity: $itemId")
                    if (entity != null && _state.value !is EditMeasureUiState.Processing) {
                        _state.value = EditMeasureUiState.Loaded(
                            date = entity.date,
                            weight = entity.weight,
                            unit = entity.unit.toWeightUnitUi()
                        )
                    }
                }
        }
    }

    fun onAction(editAction: EditAction) {
        when(editAction) {
            EditAction.OnCancelDelete -> onCancelDelete()
            EditAction.OnConfirmDelete -> onConfirmDelete()
            EditAction.OnDeleteAction -> onDeleteAction()
            EditAction.OnSaveAction -> onSaveAction()
            is EditAction.OnUpdateMeasure -> updateMeasure(editAction.newMeasure)
            EditAction.OnDismissAction -> onDismissAction()
            EditAction.ToggleWeightUnit -> onToggleWeightUnit()
        }
    }

    private fun onDismissAction() {
        viewModelScope.launch {
            sendCloseDialogEvent()
        }
    }

    private fun updateMeasure(weight: BigDecimal) {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(
                weight = weight
            )
        }
    }

    /*
    fun updateWeightUnit(weightUnit: WeightUnitUi) {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(
                unit = weightUnit
            )
        }
    }
    */


    private fun onSaveAction() {
        val current = _state.value
        if (current !is EditMeasureUiState.Loaded) return

        _state.value = EditMeasureUiState.Processing
        launchWithErrorHandling(
            onError = {
                _state.value = current
            }
        ) {
            repository.update(
                id = itemId,
                date = current.date,
                weight = current.weight,
                unit = current.unit.toWeightUnit()
            )
            sendInfo(R.string.successfully_saved)
            sendCloseDialogEvent()
        }
    }

    private fun onDeleteAction() {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(showDeleteConfirm = true)
        }
    }

    private fun onCancelDelete() {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(showDeleteConfirm = false)
        }
    }

    private fun onConfirmDelete() {
        val current = _state.value
        if (current !is EditMeasureUiState.Loaded) return

        _state.value = EditMeasureUiState.Processing
        launchWithErrorHandling(
            onError = {
                _state.value = current
            }
        ) {
            repository.delete(itemId)
            sendInfo(R.string.successfully_deleted)
            sendCloseDialogEvent()
        }
    }

    private fun onToggleWeightUnit() {
        val current = _state.value
        if (current is EditMeasureUiState.Loaded) {
            _state.value = current.copy(unit = if (current.unit == WeightUnitUi.KG) WeightUnitUi.LB else WeightUnitUi.KG)
        }
    }

}
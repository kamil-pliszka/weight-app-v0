package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.core.presentation.DefaultUiEventOwner
import com.pl.myweightapp.core.presentation.UiEventOwner
import com.pl.myweightapp.core.presentation.launchSafely
import com.pl.myweightapp.core.presentation.sendInfo
import com.pl.myweightapp.data.repository.WeightMeasureRepository
import com.pl.myweightapp.feature.history.WeightUnitUi
import com.pl.myweightapp.feature.history.toWeightUnit
import com.pl.myweightapp.feature.history.toWeightUnitUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    data object Saving : EditMeasureUiState

    //data object Saved : EditMeasureUiState
    //data object Deleted : EditMeasureUiState
    //data class Error(val message: String) : EditMeasureUiState
}

@HiltViewModel
class EditMeasureViewModel @Inject constructor(
    //private val itemId: Long,
    private val repository: WeightMeasureRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    private val itemId: Long = savedStateHandle["itemId"] ?: error("itemId is required")

    companion object {
        private const val TAG = "EditMeasureVM"
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
                    if (entity == null) {
                        //Deleted
                        //_state.value = EditMeasureUiState.Deleted
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


    fun onSaveAction(/*closeDialogHandler : () -> Unit*/) {
        val current = _state.value
        if (current !is EditMeasureUiState.Loaded) return

        _state.value = EditMeasureUiState.Saving
        launchSafely {
            repository.update(
                id = itemId,
                date = current.date,
                weight = current.weight,
                unit = current.unit.toWeightUnit()
            )
            //_state.value = EditMeasureUiState.Saved
            //_state.value = current
            sendInfo(R.string.successfully_saved)
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
            _state.value = current.copy(showDeleteConfirm = false)
        }
    }

    fun onConfirmDelete() {
        launchSafely {
            repository.delete(itemId)
            sendInfo(R.string.successfully_deleted)
        }
    }

}
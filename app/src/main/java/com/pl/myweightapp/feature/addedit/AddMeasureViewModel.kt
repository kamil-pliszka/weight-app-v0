package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.core.presentation.DefaultUiEventOwner
import com.pl.myweightapp.core.presentation.UiEventOwner
import com.pl.myweightapp.core.presentation.launchSafely
import com.pl.myweightapp.core.util.toInstant
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.domain.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject


data class AddMeasureState(
    //val showDialog: Boolean = false,
    val isLoading: Boolean = false,
    val lastWeight: BigDecimal? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val currentWeightMeasure: BigDecimal = "56.7".toBigDecimal(),
    //choose date dialog state
    val showDateDialog: Boolean = false,
    val choosenDate: LocalDate = LocalDate.now(),
)

sealed interface AddAction {
    object OnShowDateDialogAction : AddAction
    object OnDialogConfirmAction : AddAction
    data class UpdateCurrentMeasure(val measure: BigDecimal) : AddAction
    object OnCloseDateDialog : AddAction
    data class UpdateChoosenDate(val date: LocalDate) : AddAction
    object OnDismissAction : AddAction
    object ToggleWeightUnit : AddAction
}

@HiltViewModel
class AddMeasureViewModel @Inject constructor(
    private val repository: WeightMeasureRepository,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    private val _state = MutableStateFlow(AddMeasureState())
    val state = _state.asStateFlow()

    private val _navEvents = MutableSharedFlow<AddEditMeasureEvent>(
        extraBufferCapacity = 1
    )
    val navEvents = _navEvents.asSharedFlow()

    private suspend fun sendCloseDialogEvent() {
        _navEvents.emit(AddEditMeasureEvent.CloseDialog)
    }

    init {
        Log.d(TAG, "init")
        launchSafely {
            _state.update { it.copy(isLoading = true) }
            repository.findLastWeightMeasureAndUnit()?.let { (value, unit) ->
                Log.d(TAG, "LastWeightMeasure: $value, unit: $unit")
                _state.update { it.copy(lastWeight = value, currentWeightMeasure = value, weightUnit = unit) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onAction(addAction: AddAction) {
        when (addAction) {
            AddAction.OnCloseDateDialog -> onCloseDateDialog()
            AddAction.OnDialogConfirmAction -> onDialogConfirmAction()
            AddAction.OnShowDateDialogAction -> onShowDateDialogAction()
            is AddAction.UpdateChoosenDate -> updateChoosenDate(addAction.date)
            is AddAction.UpdateCurrentMeasure -> updateCurrentMeasure(addAction.measure)
            AddAction.OnDismissAction -> onDismissAction()
            AddAction.ToggleWeightUnit -> onToggleWeightUnit()
        }
    }

    private fun onDismissAction() {
        viewModelScope.launch {
            sendCloseDialogEvent()
        }
    }

    private fun onDialogConfirmAction() {
        val currentMeasure = state.value.currentWeightMeasure
        val choosenDate = state.value.choosenDate
        val instantDate = if (choosenDate == LocalDate.now()) {
            Instant.now() //z czasem
        } else {
            choosenDate.toInstant()
        }
        launchSafely {
            repository.insertMeasure(
                date = instantDate,
                weight = currentMeasure,
                unit = state.value.weightUnit
            )
            //_state.update { it.copy(showDialog = false) }
            sendCloseDialogEvent()
        }
    }

//    init {
//        loadLastWeight()
//    }
//
//    private fun loadLastWeight() {
//        launchWithErrorHandling {
//            val value = withContext(Dispatchers.IO) {
//                AppModule.provideWeightMeasureRepository().findLastWeightMeasure()
//            }
//            if (value != null) {
//                _state.update { it.copy(lastWeight = value, currentWeightMeasure = value) }
//            }
//        }
//    }
//
//    fun onDialogDismissAction() {
//        _state.update { it.copy(showDialog = false) }
//    }

    private fun onShowDateDialogAction() {
        _state.update { it.copy(showDateDialog = true) }
    }

    private fun onCloseDateDialog() {
        _state.update { it.copy(showDateDialog = false) }
    }

    private fun updateChoosenDate(date: LocalDate) {
        _state.update { it.copy(choosenDate = date) }
    }

    private fun updateCurrentMeasure(newMeasure: BigDecimal) {
        _state.update { it.copy(currentWeightMeasure = newMeasure) }
    }

    private fun onToggleWeightUnit() {
        _state.update { it.copy(weightUnit = if (state.value.weightUnit == WeightUnit.KG) WeightUnit.LB else WeightUnit.KG) }
    }

}
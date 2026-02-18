package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.app.di.AppModule
import com.pl.myweightapp.core.domain.WeightUnit
import com.pl.myweightapp.core.presentation.UiEvent
import com.pl.myweightapp.core.util.exceptionToString
import com.pl.myweightapp.core.util.toInstant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException


data class AddMeasureState(
    val showDialog: Boolean = false,
    val lastWeight: BigDecimal? = null,
    val currentWeightMeasure: BigDecimal = "56.7".toBigDecimal(),
    //choose date dialog state
    val showDateDialog: Boolean = false,
    val choosenDate: LocalDate = LocalDate.now(),
)

class AddMeasureViewModel : ViewModel() {
    companion object {
        private const val TAG = "AddMeasureVM"
    }
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val _state = MutableStateFlow(AddMeasureState())
    val state = _state.asStateFlow()

    private fun launchWithErrorHandling(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e("AddMeasureViewModel", "error", e)
                //withContext(Dispatchers.Main) {
                    _events.send(UiEvent.IOError(exceptionToString(e)))
                //}
            }
        }
    }

    fun onShowDialogAction() {
        launchWithErrorHandling {
            val value = AppModule.provideWeightMeasureRepository().findLastWeightMeasure()
            if (value != null) {
                _state.update { it.copy(lastWeight = value, currentWeightMeasure = value) }
            }
            _state.update { it.copy(showDialog = true) }
        }
        Log.d(TAG,"ShowDialogAction")
    }


    fun onDialogConfirmAction() {
        val currentMeasure = state.value.currentWeightMeasure
        val choosenDate = state.value.choosenDate
        val instantDate = if (choosenDate == LocalDate.now()) {
            Instant.now() //z czasem
        } else {
            choosenDate.toInstant()
        }
        launchWithErrorHandling {
            AppModule.provideWeightMeasureRepository().insertMeasure(
                date = instantDate,
                weight = currentMeasure,
                unit = WeightUnit.KG
            )
            _state.update { it.copy(showDialog = false) }
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

    fun onDialogDismissAction() {
        _state.update { it.copy(showDialog = false) }
    }

    fun onShowDateDialogAction() {
        _state.update { it.copy(showDateDialog = true) }
    }

    fun onCloseDateDialog() {
        _state.update { it.copy(showDateDialog = false) }
    }

    fun updateChoosenDate(date: LocalDate) {
        _state.update { it.copy(choosenDate = date) }
    }

    fun updateCurrentMeasure(newMeasure: BigDecimal) {
        _state.update { it.copy(currentWeightMeasure = newMeasure) }
    }

}
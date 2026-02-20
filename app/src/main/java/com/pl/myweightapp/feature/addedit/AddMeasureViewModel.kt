package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.lifecycle.ViewModel
import com.pl.myweightapp.core.domain.WeightUnit
import com.pl.myweightapp.core.presentation.DefaultUiEventOwner
import com.pl.myweightapp.core.presentation.UiEventOwner
import com.pl.myweightapp.core.presentation.launchSafely
import com.pl.myweightapp.core.util.toInstant
import com.pl.myweightapp.data.repository.WeightMeasureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject


data class AddMeasureState(
    //val showDialog: Boolean = false,
    val isLoading : Boolean = false,
    val lastWeight: BigDecimal? = null,
    val currentWeightMeasure: BigDecimal = "56.7".toBigDecimal(),
    //choose date dialog state
    val showDateDialog: Boolean = false,
    val choosenDate: LocalDate = LocalDate.now(),
)

@HiltViewModel
class AddMeasureViewModel @Inject constructor(
    private val repository: WeightMeasureRepository
): ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private const val TAG = "AddMeasureVM"
    }
    private val _state = MutableStateFlow(AddMeasureState())
    val state = _state.asStateFlow()

    init {
        Log.d(TAG,"init")
        launchSafely {
            _state.update { it.copy(isLoading = true) }
            val value = repository.findLastWeightMeasure()
            Log.d(TAG,"LastWeightMeasure: $value")
            if (value != null) {
                _state.update { it.copy(lastWeight = value, currentWeightMeasure = value) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }


    fun onDialogConfirmAction() {
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
                unit = WeightUnit.KG
            )
            //_state.update { it.copy(showDialog = false) }
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
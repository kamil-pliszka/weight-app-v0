package com.pl.myweightapp.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.core.util.exceptionToString
import com.pl.myweightapp.domain.AppSettingsService
import com.pl.myweightapp.domain.DisplayPeriod
import com.pl.myweightapp.domain.UserProfileRepository
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.domain.chart.ChartImageManager
import com.pl.myweightapp.domain.usecase.ComputeHomeStateUseCase
import com.pl.myweightapp.domain.usecase.GenerateChartImageUseCase
import com.pl.myweightapp.feature.common.DefaultUiEventOwner
import com.pl.myweightapp.feature.common.UiEventOwner
import com.pl.myweightapp.feature.common.launchWithErrorHandling
import com.pl.myweightapp.feature.common.sendError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface Action {
    data class OnChangeChartDimensionsAction(val widthPx: Int, val heightPx: Int) : Action
    data class OnChangePeriod(val period: DisplayPeriod) : Action
    data class OnChangeMovingAverages(val ma1: Int?, val ma2: Int?) : Action
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    weightRepo: WeightMeasureRepository,
    profileRepo: UserProfileRepository,
    private val appSettingsService: AppSettingsService,
    private val computeHomeStateUseCase: ComputeHomeStateUseCase,
    private val generateChartImageUseCase: GenerateChartImageUseCase,
    private val chartImageManager: ChartImageManager,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    /*private suspend inline fun <T> withProcessing(
        crossinline block: suspend () -> T
    ): T {
        return try {
            _state.update { it.copy(isProcessing = true) }
            block()
        } finally {
            _state.update { it.copy(isProcessing = false) }
        }
    }*/


    fun onAction(action: Action) {
        when (action) {
            is Action.OnChangeChartDimensionsAction -> {
                Log.d(TAG, "change dimensions: ${action.widthPx}x${action.heightPx}")
                _state.update {
                    it.copy(
                        chartWidthPx = action.widthPx,
                        chartHeightPx = action.heightPx
                    )
                }
            }

            is Action.OnChangePeriod -> changePeriod(action.period)
            is Action.OnChangeMovingAverages -> changeMovingAverages(action.ma1, action.ma2)
        }
    }

    init {
        tryToLoadFromFile()

        // Połącz Flow w jeden
        combine(
            weightRepo.observeWeightMeasureHistory(),
            profileRepo.observeProfile(),
            appSettingsService.settingsFlow
        ) { history, profile, settings ->
            Log.d(TAG, "From combine: ${history.size}, settings: $settings, profile: $profile")
            // przetwarzanie do UI modelu
            Triple(history, profile, settings)
        }.onStart {
            _state.update { it.copy(isLoading = true) }
        }.onEach { (history, profile, settings) ->
            val res = computeHomeStateUseCase(
                settings,
                history.reversed(),
                profile?.weightUnit,
                profile?.targetWeight
            )
            _state.update {
                it.copy(
                    isLoading = false,
                    unit = res.unit,
                    period = res.period,
                    movingAverage1 = res.movingAverage1,
                    movingAverage2 = res.movingAverage2,
                    useEmbeddedChart = res.useEmbeddedChart,
                    startWeight = res.startWeight,
                    currentWeight = res.currentWeight,
                    destinationWeight = res.destinationWeight,
                    periodWeightChange = res.periodWeightChange,
                    toTargetWeight = res.toTargetWeight,
                    chartData = res.chartData
                )
            }
        }.catch { ex ->
            sendError(R.string.error_msg_prefix, exceptionToString(ex))
        }.launchIn(viewModelScope)


        // generowanie obrazka który będzie wykorzystany przy inicjalnym ładowaniu VM
        // do zastanowienia czy zostawić
        combine(
            _state.map { it.chartData },
            _state.map { it.chartWidthPx to it.chartHeightPx },
            _state.map { it.useEmbeddedChart }
        ) { data, dims, embedded ->
            if (embedded) return@combine null
            Triple(data, dims.first, dims.second)
        }
            .distinctUntilChanged()
            .onEach { triple ->
                if (triple == null) return@onEach
                val (data, width, height) = triple
                val image = generateChartImageUseCase(
                    chartData = data, widthPx = width, heightPx = height
                )
                _state.update { it.copy(chartImage = image) }
                if (image != null) {
                    withContext(Dispatchers.IO) {
                        chartImageManager.export(image)
                    }
                }
            }
            .launchIn(viewModelScope)

    }

    private fun tryToLoadFromFile() {
        launchWithErrorHandling {
            val chartImage = chartImageManager.import()
            if (chartImage != null) {
                Log.d(TAG, "loaded chart from file")
                //val imageBitmap = chartImageDecoder.decode(chartImage)
                _state.update { it.copy(chartImage = chartImage) }
            }
        }
    }


    private fun changePeriod(period: DisplayPeriod) {
        launchWithErrorHandling {
            appSettingsService.changePeriod(period.name)
        }
    }

    private fun changeMovingAverages(ma1: Int?, ma2: Int?) {
        launchWithErrorHandling {
            appSettingsService.changeMovingAverages(
                if (ma1 == null || ma1 <= 1) null else ma1,
                if (ma2 == null || ma2 <= 1) null else ma2
            )
        }
    }
}
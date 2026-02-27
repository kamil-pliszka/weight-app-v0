package com.pl.myweightapp.feature.home

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.core.presentation.DefaultUiEventOwner
import com.pl.myweightapp.core.presentation.UiEventOwner
import com.pl.myweightapp.core.presentation.launchSafely
import com.pl.myweightapp.core.presentation.sendError
import com.pl.myweightapp.core.util.exceptionToString
import com.pl.myweightapp.core.util.toInstant
import com.pl.myweightapp.core.util.toLocalDate
import com.pl.myweightapp.domain.AppSettings
import com.pl.myweightapp.domain.AppSettingsService
import com.pl.myweightapp.domain.DisplayPeriod
import com.pl.myweightapp.domain.ResourceProvider
import com.pl.myweightapp.domain.UserProfileRepository
import com.pl.myweightapp.domain.WeightMeasure
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.domain.WeightUnit
import com.pl.myweightapp.domain.chart.ChartData
import com.pl.myweightapp.domain.chart.ChartImageExporter
import com.pl.myweightapp.domain.chart.ChartImageImporter
import com.pl.myweightapp.domain.chart.ChartLabels
import com.pl.myweightapp.domain.convertValueTo
import com.pl.myweightapp.domain.convertWeightTo
import com.pl.myweightapp.domain.usecase.GenerateWeightChartDataUseCase
import com.pl.myweightapp.feature.home.chart.ChartImageDecoder
import com.pl.myweightapp.feature.home.chart.ChartRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject

@Immutable
data class UiState(
    val isLoading: Boolean = false,
    val useEmbeddedChart: Boolean = Constants.DEFAULT_USE_EMBEDDED_CHART,
    val isProcessing: Boolean = false,
    val chartBitmap: ImageBitmap? = null,
    val unit: WeightUnit = WeightUnit.KG,
    val period: DisplayPeriod = DisplayPeriod.P2M,
    val movingAverage1: Int? = null,
    val movingAverage2: Int? = null,
    val startWeight: Float? = null,
    val currentWeight: Float? = null,
    val destinationWeight: Float? = null,
    val periodWeightChange: Float? = null,
    val toDestinationWeight: Float? = null,
    val chartWidthPx: Int = 0,
    val chartHeightPx: Int = 0,
    val chartData: ChartData = ChartData(),
    val chartLabels: ChartLabels = ChartLabels(),
)

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
    private val resourceProvider: ResourceProvider,
    private val generateWeightChartDataUseCase: GenerateWeightChartDataUseCase,
    private val chartRenderer: ChartRenderer,
    private val chartImageExporter: ChartImageExporter,
    private val chartImageImporter: ChartImageImporter,
    private val chartImageDecoder: ChartImageDecoder,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private suspend inline fun <T> withProcessing(
        crossinline block: suspend () -> T
    ): T {
        return try {
            _state.update { it.copy(isProcessing = true) }
            block()
        } finally {
            _state.update { it.copy(isProcessing = false) }
        }
    }


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
                if (!state.value.useEmbeddedChart) {
                    generateChartAsImage()
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
            Log.d(TAG, "dim: ${state.value.chartWidthPx}x${state.value.chartHeightPx}")
            // przetwarzanie do UI modelu
            Triple(history, profile, settings)
        }.onStart {
            _state.update { it.copy(isLoading = true) }
        }.onEach { (history, profile, settings) ->
            prepareDependentStateValues(
                settings,
                history.reversed(),
                profile?.weightUnit,
                profile?.targetWeight
            )
            if (!settings.embeddedChart) {
                generateChartAsImage()
            }
        }.catch { ex ->
            sendError(R.string.error_msg_prefix, exceptionToString(ex))
        }.launchIn(viewModelScope)
    }

    private fun prepareDependentStateValues(
        settings: AppSettings,
        measures: List<WeightMeasure>, //tutaj pomiary już posortowane rosnąco względem daty
        weightUnit: WeightUnit?,
        targetWeight: BigDecimal?
    ) {
        val measureUnit: WeightUnit = weightUnit ?: WeightUnit.KG
        val period: DisplayPeriod = runCatching {
            DisplayPeriod.valueOf(settings.displayPeriod)
        }.getOrDefault(DisplayPeriod.P2M)
        val currentMeasure = measures.lastOrNull()

        val periodStartMeasure = currentMeasure?.let {
            if (period == DisplayPeriod.ALL) {
                measures.firstOrNull() ?: currentMeasure
            } else {
                val startPeriodDate = with(currentMeasure.date.toLocalDate()) {
                    when (period) {
                        DisplayPeriod.P2W -> minusWeeks(2)
                        DisplayPeriod.P1M -> minusMonths(1)
                        DisplayPeriod.P2M -> minusMonths(2)
                        DisplayPeriod.P3M -> minusMonths(3)
                        DisplayPeriod.P6M -> minusMonths(6)
                        DisplayPeriod.P1Y -> minusYears(1)
                        DisplayPeriod.P2Y -> minusYears(2)
                        DisplayPeriod.P3Y -> minusYears(3)
                    }
                }.plusDays(1)
                Log.d(TAG, "PeriodDate before: $startPeriodDate")
                val startPeriodInstant = startPeriodDate.toInstant()
                measures.lastOrNull {
                    it.date.isBefore(startPeriodInstant)
                } ?: measures.first()
            }
        }
        Log.d(TAG, "Period start measure: $periodStartMeasure")

        val currentWeight = currentMeasure?.convertWeightTo(measureUnit)
        val destWeight = targetWeight?.convertValueTo(measureUnit, measureUnit)
        val periodStartWeight = periodStartMeasure?.convertWeightTo(measureUnit)
        val toTarget =
            if (currentWeight != null && destWeight != null) currentWeight - destWeight else null
        val periodWeightChange =
            if (currentWeight != null && periodStartWeight != null) currentWeight - periodStartWeight else null
        val startIdx = periodStartMeasure?.let { m ->
            measures.indexOf(m)
        } ?: 0

        _state.update {
            it.copy(
                isLoading = false,
                unit = measureUnit,
                period = period,
                movingAverage1 = settings.ma1,
                movingAverage2 = settings.ma2,
                useEmbeddedChart = settings.embeddedChart,
                startWeight = periodStartWeight,
                currentWeight = currentWeight,
                destinationWeight = destWeight,
                periodWeightChange = periodWeightChange,
                toDestinationWeight = toTarget,
                chartData = generateWeightChartDataUseCase(
                    totalWeightMeasures = measures,
                    unit = measureUnit,
                    startIdx = startIdx,
                    movingAverage1 = settings.ma1,
                    movingAverage2 = settings.ma2,
                    targetValue = destWeight
                ),
                chartLabels = prepareChartLabels(),
            )
        }
        Log.d(TAG, "Updated state")
    }

    private fun prepareChartLabels(): ChartLabels {
        return ChartLabels(
            weightLabel = resourceProvider.getString(R.string.chart_weight),
            averageLabel = resourceProvider.getString(R.string.chart_average),
            targetLabel = resourceProvider.getString(R.string.chart_target),
            mavPrefixLabel = resourceProvider.getString(R.string.chart_mav)
        )
    }

    private fun generateChartAsImage() {
        val currentState = state.value
        Log.d(TAG, "Generate chart ${currentState.chartWidthPx}x${currentState.chartHeightPx}")
        Log.d(TAG, "Measures: ${currentState.chartData.measures.size}")
        launchSafely {
            withProcessing {
                if (currentState.chartData.measures.isNotEmpty()
                    && currentState.chartWidthPx > 0
                    && currentState.chartHeightPx > 0
                ) {
                    // Renderer zwraca już ChartImage (ByteArray)
                    val chartImage = chartRenderer.render(
                        chartData = currentState.chartData,
                        chartLabels = currentState.chartLabels,
                        widthPx = currentState.chartWidthPx,
                        heightPx = currentState.chartHeightPx,
                    )
                    // Dekodowanie do Bitmap WYŁĄCZNIE w presentation layer
                    val imageBitmap = chartImageDecoder.decode(chartImage)
                    _state.update { it.copy(chartBitmap = imageBitmap) }
                    // Eksport bez ponownej kompresji
                    chartImageExporter.export(chartImage)
                    //delay(5000L)
                } else {
                    _state.update {
                        it.copy(chartBitmap = null)
                    }
                }
            }
        }
    }

    private fun tryToLoadFromFile() {
        launchSafely {
            val chartImage = chartImageImporter.import()
            if (chartImage != null) {
                Log.d(TAG, "loaded chart from file")
                val imageBitmap = chartImageDecoder.decode(chartImage)
                _state.update { it.copy(chartBitmap = imageBitmap) }
            }
        }
    }


    private fun changePeriod(period: DisplayPeriod) {
        launchSafely {
            appSettingsService.changePeriod(period.name)
        }
    }

    private fun changeMovingAverages(ma1: Int?, ma2: Int?) {
        launchSafely {
            appSettingsService.changeMovingAverages(
                if (ma1 == null || ma1 <= 1) null else ma1,
                if (ma2 == null || ma2 <= 1) null else ma2
            )
        }
    }
}
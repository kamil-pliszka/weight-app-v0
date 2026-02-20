package com.pl.myweightapp.feature.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.app.di.AppModule
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.core.domain.WeightUnit
import com.pl.myweightapp.core.presentation.DefaultUiEventOwner
import com.pl.myweightapp.core.presentation.UiEventOwner
import com.pl.myweightapp.core.presentation.launchSafely
import com.pl.myweightapp.core.util.kgToLbs
import com.pl.myweightapp.core.util.lbsToKg
import com.pl.myweightapp.core.util.toFloat1
import com.pl.myweightapp.core.util.toInstant
import com.pl.myweightapp.core.util.toLocalDate
import com.pl.myweightapp.data.local.UserProfileEntity
import com.pl.myweightapp.data.local.WeightMeasureEntity
import com.pl.myweightapp.data.repository.sortWeightMeasureHistory
import com.pl.myweightapp.feature.home.chart.Measurement
import com.pl.myweightapp.feature.home.chart.generateChartBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal

@Immutable
data class UiState(
    val isLoading: Boolean = false,
    val useEmbeddedChart: Boolean = Constants.DEFAULT_USE_EMBEDDED_CHART,
    val isProcessing: Boolean = false,
    //val progress: Float = 0f, // 0..1
    val chartBitmap: ImageBitmap? = null,
    val weightHistory: List<WeightMeasureEntity> = listOf(),
    val profile: UserProfileEntity? = null,
    val unit: WeightUnit = WeightUnit.KG,
    val period: DisplayPeriod = DisplayPeriod.P2M,
    val movingAverage1: Int? = null,
    val movingAverage2: Int? = null,
    val startWeight: Float? = null,
    val currentWeight: Float? = null,
    val destinationWeight: Float? = null,
    val periodWeightChange: Float? = null,
    val toDestinationWeight: Float? = null,
    val periodStartEntity: WeightMeasureEntity? = null,
    val chartWidthPx: Int = 0,
    val chartHeightPx: Int = 0,
)

sealed interface Action {
    data class OnChangeChartDimensionsAction(val widthPx: Int, val heightPx: Int) : Action
    data class OnChangePeriod(val period: DisplayPeriod) : Action
    data class OnChangeMovingAverages(val ma1: Int?, val ma2: Int?) : Action
}

class HomeViewModel : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private const val TAG = "HomeVM"
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private val weightRepo = AppModule.provideWeightMeasureRepository()
    private val profileRepo = AppModule.provideUserProfileRepository()

    private val appSettingsManager = AppModule.provideAppSettingsManager()

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
                generateChart()
            }

            is Action.OnChangePeriod -> changePeriod(action.period)
            is Action.OnChangeMovingAverages -> changeMovingAverages(action.ma1, action.ma2)
        }
    }

    init {
        tryToLoadFromFile()
    }

    init {
        // Połącz Flow w jeden
        combine(
            weightRepo.observeWeightMeasureHistory(),
            profileRepo.profile,
            appSettingsManager.settingsFlow
        ) { history, profile, settings ->
            Log.d(TAG, "From combine: ${history.size}, settings: $settings, profile: $profile")
            Log.d(TAG, "dim: ${state.value.chartWidthPx}x${state.value.chartHeightPx}")
            // przetwarzanie do UI modelu
            Triple(sortWeightMeasureHistory(history), profile, settings)
        }.onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { (history, profile, settings) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        weightHistory = history,
                        profile = profile,
                        period = runCatching { DisplayPeriod.valueOf(settings.displayPeriod) }.getOrDefault(DisplayPeriod.P2M),
                        movingAverage1 = settings.ma1,
                        movingAverage2 = settings.ma2,
                        useEmbeddedChart = settings.embeddedChart,
                    )
                }
                prepareDependentStateValues()
                generateChart()
            }
            .launchIn(viewModelScope)
    }

    private fun prepareDependentStateValues() {
        val measurementUnit: WeightUnit = state.value.profile?.weightUnit ?: WeightUnit.KG
        val period: DisplayPeriod = state.value.period
        val startEntity = state.value.weightHistory.lastOrNull()
        val currentEntity = state.value.weightHistory.firstOrNull()
        val destEntity = state.value.profile?.targetWeight

        val periodStartEntity = currentEntity?.let {
            if (period == DisplayPeriod.ALL) {
                startEntity ?: currentEntity
            } else {
                val startPeriodDate = with(currentEntity.date.toLocalDate()) {
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
                state.value.weightHistory.firstOrNull { it.date.isBefore(startPeriodInstant) }
            }
        }
        Log.d(TAG, "Period start entity: $periodStartEntity")

//        val startWeight =
//            startEntity?.weight?.convertWeightValueTo(startEntity.unit, measurementUnit)
        val currentWeight =
            currentEntity?.weight?.convertWeightValueTo(currentEntity.unit, measurementUnit)
        val destWeight = destEntity?.convertWeightValueTo(measurementUnit, measurementUnit)
        val periodStartWeight =
            periodStartEntity?.weight?.convertWeightValueTo(periodStartEntity.unit, measurementUnit)
        val toDestinationWeight =
            if (currentWeight != null && destWeight != null) currentWeight - destWeight else null
        val periodWeightChange =
            if (currentWeight != null && periodStartWeight != null) currentWeight - periodStartWeight else null

        _state.update {
            it.copy(
                unit = measurementUnit,
                period = period,
                startWeight = periodStartWeight,
                currentWeight = currentWeight,
                destinationWeight = destWeight,
                periodWeightChange = periodWeightChange,
                toDestinationWeight = toDestinationWeight,
                periodStartEntity = periodStartEntity,
            )
        }
        Log.d(TAG, "Updated state")
    }

    private fun generateChart() {
        if (state.value.useEmbeddedChart) {
            Log.d(TAG,"useEmbeddedChart: skip generateChart")
            return
        }
        Log.d(TAG, "Generate chart ${state.value.chartWidthPx}x${state.value.chartHeightPx}")
        Log.d(TAG, "Proifle: ${state.value.profile}")
        Log.d(TAG, "Measurements: ${state.value.weightHistory.size}")
        launchSafely {
            withProcessing {
                val history = state.value.weightHistory.reversed()
                val startIdx = state.value.periodStartEntity?.let {
                    history.indexOf(it)
                } ?: 0
                val measurements = prepareWeightMeasurements(
                    history,
                    state.value.unit,
                )
                if (measurements.isNotEmpty() && state.value.chartWidthPx > 0 && state.value.chartHeightPx > 0) {
                    val bitmap = generateChartBitmap(
                        context = AppModule.provideContext(),
                        totalMeasurements = measurements,
                        startIdx = startIdx,
                        widthPx = state.value.chartWidthPx,
                        heightPx = state.value.chartHeightPx,
                        destinationValue = state.value.destinationWeight,
                        movingAverage1 = state.value.movingAverage1,
                        movingAverage2 = state.value.movingAverage2,
                    )
                    Log.d(
                        TAG,
                        "Finished chart generation, got bitmap: ${bitmap.width}x${bitmap.height}"
                    )

                    _state.update {
                        it.copy(chartBitmap = bitmap.asImageBitmap())
                    }
                    exportChart(bitmap)
                    delay(5000L)
                } else {
                    _state.update {
                        it.copy(chartBitmap = null)
                    }
                }
            }
        }
    }


    // w ViewModel
    private fun exportChart(bitmap: Bitmap) {
        launchSafely {
            Log.d(TAG, "Exporting chart")
            val file = saveBitmapToFileAsync(
                AppModule.provideContext(),
                bitmap,
                Constants.WEIGHT_CHART_FILENAME
            )
            Log.d(TAG, "Zapisano wykres do: ${file.absolutePath}")
        }
    }

    private suspend fun saveBitmapToFileAsync(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): File = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, fileName) // lub context.cacheDir
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        file
    }

    private fun tryToLoadFromFile() {
        launchSafely {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "tryToLoadFromFile")
                val context = AppModule.provideContext()
                val file = File(context.filesDir, Constants.WEIGHT_CHART_FILENAME)
                val bitmap = loadBitmapFromFile(file)
                if (bitmap != null) {
                    Log.d(TAG, "loaded chart from file")
                    _state.update {
                        it.copy(chartBitmap = bitmap.asImageBitmap())
                    }
                }
                Log.d(TAG, "tryToLoadFromFile-end")
            }
        }
    }

    private fun loadBitmapFromFile(file: File): Bitmap? {
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    private fun changePeriod(period: DisplayPeriod) {
        launchSafely {
            appSettingsManager.changePeriod(period.name)
        }
    }

    private fun changeMovingAverages(ma1: Int?, ma2: Int?) {
        launchSafely {
            appSettingsManager.changeMovingAverages(
                if (ma1 == null || ma1 <= 1) null else ma1,
                if (ma2 == null || ma2 <= 1) null else ma2
            )
        }
    }
}

fun prepareWeightMeasurements(
    history: List<WeightMeasureEntity>,
    measurementUnit: WeightUnit
): List<Measurement> {
    return history.map { entity ->
        Measurement(
            entity.date,
            entity.weight.convertWeightValueTo(entity.unit, measurementUnit)
        )
    }
}

private fun BigDecimal.convertWeightValueTo(srcUnit: WeightUnit, dstUnit: WeightUnit): Float {
    val weight = this.toFloat1()
    return when {
        srcUnit == dstUnit -> weight
        dstUnit == WeightUnit.KG && srcUnit == WeightUnit.LB -> weight.lbsToKg()
        dstUnit == WeightUnit.LB && srcUnit == WeightUnit.KG -> weight.kgToLbs()
        else -> error("Unknown conversion from: $srcUnit to: $dstUnit")
    }
}

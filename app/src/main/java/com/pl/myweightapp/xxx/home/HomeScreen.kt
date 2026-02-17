package com.pl.myweightapp.xxx.home

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.myweightapp.R
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents

private const val TAG = "HomeScreen"

@Preview(name = "processing")
@Composable
fun HomeScreenPreview() {
    HomeScreenContentPortrait(
        state = UiState(isProcessing = true),
        onChangePeriod = { },
        onChangeMovingAverages = { _, _ -> },
        onChangeChartDimensions = { _, _ -> },
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val messageErrorPrefix = stringResource(R.string.error_msg_prefix)
    ObserveAsEvents(viewModel.events) { event ->
        Log.d(TAG, "got event: $event")
        when (event) {
            is UiEvent.Error -> snackbarHostState.showSnackbar(
                messageErrorPrefix + event.message,
                duration = SnackbarDuration.Long
            )

            is UiEvent.Info -> snackbarHostState.showSnackbar(event.message)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Log.d(TAG, "isLandscape: $isLandscape")

    if (isLandscape) {
        HomeScreenContentLandscape(
            modifier = modifier,
            state = state,
            onChangePeriod = { viewModel.onAction(Action.OnChangePeriod(it)) },
            onChangeMovingAverages = { ma1, ma2 ->
                viewModel.onAction(Action.OnChangeMovingAverages(ma1, ma2))
            },
            onChangeChartDimensions = { widthPx, heightPx ->
                viewModel.onAction(Action.OnChangeChartDimensionsAction(widthPx, heightPx))
            }
        )
    } else {
        HomeScreenContentPortrait(
            modifier = modifier,
            state = state,
            onChangePeriod = { viewModel.onAction(Action.OnChangePeriod(it)) },
            onChangeMovingAverages = { ma1, ma2 ->
                viewModel.onAction(Action.OnChangeMovingAverages(ma1, ma2))
            },
            onChangeChartDimensions = { widthPx, heightPx ->
                viewModel.onAction(Action.OnChangeChartDimensionsAction(widthPx, heightPx))
            }
        )
    }
}


@Composable
fun MovingAveragesComponent(
    modifier: Modifier = Modifier,
    state: UiState,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
) {

    var showMaPopup by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (showMaPopup) 180f else 0f)
    val density = LocalDensity.current
    val offsetY = with(density) { (-48).dp.roundToPx() }
    Button(
        onClick = { showMaPopup = !showMaPopup },
        modifier = modifier
    ) {
        Text("MA: ${state.movingAverage1 ?: "–"} / ${state.movingAverage2 ?: "–"}")
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.rotate(rotation)
        )
    }

    if (showMaPopup) {
        MovingAveragesPopUp(
            state.movingAverage1,
            state.movingAverage2,
            Alignment.BottomStart,
            offsetY,
            onDismissRequest = { showMaPopup = false },
            onApply = { draftMa1, draftMa2 ->
                onChangeMovingAverages(draftMa1, draftMa2)
                showMaPopup = false
            }
        )
    }
}

@Composable
fun MovingAveragesPopUp(
    movingAverage1: Int?,
    movingAverage2: Int?,
    alignment: Alignment,
    offsetY: Int,
    onDismissRequest: () -> Unit,
    onApply: (Int?, Int?) -> Unit,
) {
    var draftMa1 by remember { mutableIntStateOf(movingAverage1 ?: 1) }
    var draftMa2 by remember { mutableIntStateOf(movingAverage2 ?: 1) }

    Popup(
        alignment = alignment,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true
        )
    ) {
        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(stringResource(R.string.home_moving_averages), fontWeight = FontWeight.Bold)

                MovingAverageSlider(
                    label = "MA1",
                    value = draftMa1,
                    range = 1..20,
                    onValueChange = { draftMa1 = it }
                )

                MovingAverageSlider(
                    label = "MA2",
                    value = draftMa2,
                    range = 1..60,
                    onValueChange = { draftMa2 = it }
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(stringResource(R.string.home_ma_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onApply(draftMa1, draftMa2) }
                    ) {
                        Text(stringResource(R.string.home_ma_apply))
                    }
                }
            }
        }
    }
}

@Composable
fun MovingAverageSlider(
    label: String,
    value: Int?,             // null = off
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    val isInactive = (value ?: range.first) == range.first
    val textColor = if (isInactive) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label : ${if (isInactive) "–" else value}",
            color = textColor
        )
        Slider(
            value = (value ?: range.first).toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
        )
    }
}

@Composable
fun DisplayPeriod.label(): String = when (this) {
    DisplayPeriod.P2W -> stringResource(R.string.period_2w)
    DisplayPeriod.P1M -> stringResource(R.string.period_1m)
    DisplayPeriod.P2M -> stringResource(R.string.period_2m)
    DisplayPeriod.P3M -> stringResource(R.string.period_3m)
    DisplayPeriod.P6M -> stringResource(R.string.period_6m)
    DisplayPeriod.P1Y -> stringResource(R.string.period_1y)
    DisplayPeriod.P2Y -> stringResource(R.string.period_2y)
    DisplayPeriod.P3Y -> stringResource(R.string.period_3y)
    DisplayPeriod.ALL -> stringResource(R.string.period_all)
}
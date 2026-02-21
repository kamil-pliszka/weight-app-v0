package com.pl.myweightapp.feature.home

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pl.myweightapp.R

@Preview(name = "processing")
@Composable
fun HomeScreenPortraitPreview() {
    HomeScreenContentPortrait(
        state = UiState(isProcessing = true),
        onAction = {},
//        onChangePeriod = { },
//        onChangeMovingAverages = { _, _ -> },
//        onChangeChartDimensions = { _, _ -> },
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: UiState,
    onAction: (Action) -> Unit,
    //snackbarHostState: SnackbarHostState,
    //viewModel: HomeViewModel = hiltViewModel(),
) {
    //val state by viewModel.state.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    //Log.d(TAG, "isLandscape: $isLandscape")

    if (isLandscape) {
        HomeScreenContentLandscape(
            modifier = modifier,
            state = state,
            onAction = onAction,
//            onChangePeriod = { onAction(Action.OnChangePeriod(it)) },
//            onChangeMovingAverages = { ma1, ma2 ->
//                onAction(Action.OnChangeMovingAverages(ma1, ma2))
//            },
//            onChangeChartDimensions = { widthPx, heightPx ->
//                onAction(Action.OnChangeChartDimensionsAction(widthPx, heightPx))
//            }
        )
    } else {
        HomeScreenContentPortrait(
            modifier = modifier,
            state = state,
            onAction = onAction,
//            onChangePeriod = { onAction(Action.OnChangePeriod(it)) },
//            onChangeMovingAverages = { ma1, ma2 ->
//                onAction(Action.OnChangeMovingAverages(ma1, ma2))
//            },
//            onChangeChartDimensions = { widthPx, heightPx ->
//                onAction(Action.OnChangeChartDimensionsAction(widthPx, heightPx))
//            }
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
package com.pl.myweightapp.feature.home

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.DisplayPeriod

@Preview(name = "processing")
@Composable
fun HomeScreenPortraitPreview() {
    HomeScreenContentPortrait(
        state = HomeScreenUiState(isProcessing = true),
        onAction = {},
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeScreenUiState,
    onAction: (Action) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        HomeScreenContentLandscape(
            modifier = modifier,
            state = state,
            onAction = onAction,
        )
    } else {
        HomeScreenContentPortrait(
            modifier = modifier,
            state = state,
            onAction = onAction,
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
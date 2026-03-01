package com.pl.myweightapp.feature.home

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "processing")
@Composable
fun HomeScreenPortraitPreview() {
    HomeScreenContentPortrait(
        state = HomeUiState(isProcessing = true),
        onAction = {},
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeUiState,
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

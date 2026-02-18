package com.pl.myweightapp.xxx.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pl.myweightapp.R
import com.pl.myweightapp.xxx.EnumDropdownButton

private const val TAG = "HomeScreenPortrait"

@Composable
fun HomeScreenContentPortrait(
    modifier: Modifier = Modifier,
    state: UiState,
    //onRefresh: () -> Unit,
    onChangePeriod: (DisplayPeriod) -> Unit,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
    onChangeChartDimensions: (Int, Int) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LegendTop(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    state = state
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),   // 👈 zajmuje całą pozostałą przestrzeń
                ) {
                    ChartImageContentPortrait(
                        //modifier = Modifier,
                        //.weight(1f),   // 👈 zajmuje całą pozostałą przestrzeń
                        state = state,
                        //onClickGenerate = onRefresh,
                        onChangeChartDimensions = onChangeChartDimensions
                    )
                    BottomButtons(
                        state = state,
                        onChangePeriod = onChangePeriod,
                        onChangeMovingAverages = onChangeMovingAverages,
                    )
                }
                LegendBottom(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    state = state
                )
            }
        }

        if (state.isProcessing) {
            HomeScreenProgressBar()
        }
    }
}

@Composable
fun LegendTop(modifier: Modifier = Modifier, state: UiState) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            //.border(width = Dp.Hairline, color = Color.Cyan)
            //.height(IntrinsicSize.Min)
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            val unitStr = state.unit.name.lowercase()
            Column(horizontalAlignment = CenterHorizontally) {
                Text(stringResource(R.string.home_legend_start))
                Text("${state.startWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr")
            }
            Column(horizontalAlignment = CenterHorizontally) {
                Text(stringResource(R.string.home_legend_current))
                Text(
                    "${state.currentWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr",
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue,
                )
            }
            Column(horizontalAlignment = CenterHorizontally) {
                Text(stringResource(R.string.home_legend_target))
                Text("${state.destinationWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr")
            }
        }
    }
}

@Composable
fun LegendBottom(modifier: Modifier = Modifier, state: UiState) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            //.border(width = Dp.Hairline, color = Color.Blue)
            .padding(horizontal = 32.dp)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val unitStr = state.unit.name.lowercase()
        val periodStr = when (state.period) {
            DisplayPeriod.ALL -> ""
            else -> state.period.label()
        }
        Column(horizontalAlignment = CenterHorizontally) {
            Text(stringResource(R.string.home_legend_change, periodStr))
            Text("${state.periodWeightChange?.let { "%+.1f".format(it) } ?: "–"} $unitStr")
        }
        Column(horizontalAlignment = CenterHorizontally) {
        }
        Column(horizontalAlignment = CenterHorizontally) {
            Text(stringResource(R.string.home_legend_to_target))
            Text("${state.toDestinationWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr")
        }
    }
}

@Composable
fun ChartImageContentPortrait(
    modifier: Modifier = Modifier,
    state: UiState,
    onChangeChartDimensions: (Int, Int) -> Unit
) {
    // val painter = rememberAsyncImagePainter("file:///android_asset/my_chart.png") // Jeśli w assets
    //val painter =
    //    if (state.chartBitmap != null) BitmapPainter(state.chartBitmap) else painterResource(id = R.drawable.ic_chart_trend_down)

    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    //var chartHeightPx by remember { mutableIntStateOf(0) }
    //var chartWidthPx by remember { mutableIntStateOf(0) }
    var chartSize by remember { mutableStateOf(IntSize(0, 0)) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
            .clipToBounds()
            .onSizeChanged { size ->
                if (size != chartSize) {
                    chartSize = size
                    Log.d(TAG, "Chart size from box(port): $size")
                    if (chartSize.width > 0 && chartSize.height > 0) {
                        onChangeChartDimensions(chartSize.width, chartSize.height)
                    }
                }
            }
    ) {
        if (state.useEmbeddedChart) {
            EmbededChartComponent(
                state = state,
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    //.onSizeChanged { sz -> Log.d(TAG,"new size:box:$sz") }
                    //.border(width = Dp.Hairline, color = Color.Magenta)
                    //.clipToBounds()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterStart
            ) {
                if (state.chartBitmap != null) {
                    Image(
                        painter = BitmapPainter(state.chartBitmap),
                        contentDescription = stringResource(R.string.home_weight_graph),
                        modifier = Modifier
                            .fillMaxHeight(),
                        //.onSizeChanged { sz -> Log.d(TAG,"new size:img:$sz") }
                        contentScale = ContentScale.FillHeight
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_chart_trend_down),
                        contentDescription = stringResource(R.string.home_weight_graph),
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
fun BottomButtons(
    modifier: Modifier = Modifier,
    state: UiState,
    onChangePeriod: (DisplayPeriod) -> Unit,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        MovingAveragesComponent(
            modifier = Modifier.align(Alignment.BottomStart),
            movingAverage1 = state.movingAverage1,
            movingAverage2 = state.movingAverage2,
            onChangeMovingAverages = onChangeMovingAverages
        )
//        Button(
//            onClick = onClickGenerate,
//            modifier = Modifier.align(Alignment.BottomCenter)
//        ) {
//            Text("Generuj")
//        }
        EnumDropdownButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            selected = state.period,
            items = DisplayPeriod.entries.toTypedArray(),
            getLabel = { it.label() },
            menuWidth = 48.dp,
            onSelected = { onChangePeriod(it) },
        )
    }
}
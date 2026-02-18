package com.pl.myweightapp.xxx.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.unit.sp
import com.pl.myweightapp.R
import com.pl.myweightapp.xxx.EnumDropdownButton

private const val TAG = "HomeScreenLandscape"

@Composable
fun HomeScreenContentLandscape(
    modifier: Modifier = Modifier,
    state: UiState,
    //onRefresh: () -> Unit,
    onChangePeriod: (DisplayPeriod) -> Unit,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
    onChangeChartDimensions: (Int, Int) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
        //.border(1.dp, Color.Red)
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
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ChartImageContentLandscape(
                    modifier = Modifier.weight(1f),
                    state = state,
                    //onClickGenerate = onRefresh,
                    //onChangePeriod = onChangePeriod,
                    //onChangeMovingAverages = onChangeMovingAverages,
                    onChangeChartDimensions = onChangeChartDimensions
                )
                Spacer(modifier = Modifier.width(8.dp))  // mała przerwa
                LegendVertical(
                    state = state,
                    onChangeMovingAverages = onChangeMovingAverages,
                    onChangePeriod = onChangePeriod,
                )
            }
        }

        if (state.isProcessing) {
            HomeScreenProgressBar()
        }
    }
}


@Composable
fun ChartImageContentLandscape(
    modifier: Modifier = Modifier,
    state: UiState,
    onChangeChartDimensions: (Int, Int) -> Unit
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    var chartSize by remember { mutableStateOf(IntSize(0, 0)) }
    Box(
        modifier = modifier
            .fillMaxHeight()
            //.border(width = 1.dp, color = Color.Green)
            //.padding(4.dp)
            .clipToBounds()
            .onSizeChanged { size ->
                if (size != chartSize) {
                    chartSize = size
                    Log.d(TAG, "Chart size from box(land): $size")
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
fun LegendVertical(
    state: UiState,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
    onChangePeriod: (DisplayPeriod) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .verticalScroll(scrollState)
            //.border(width = Dp.Hairline, color = Color.Cyan)
            //.height(IntrinsicSize.Min)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            2.dp,
            alignment = Alignment.CenterVertically
        ),
    ) {
        val unitStr = state.unit.name.lowercase()
        val periodStr = when (state.period) {
            DisplayPeriod.ALL -> ""
            else -> state.period.label()
        }

        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp)
        ) {
            EnumDropdownButton(
                modifier = Modifier, //.align(Alignment.BottomEnd),
                selected = state.period,
                items = DisplayPeriod.entries.toTypedArray(),
                getLabel = { it.label() },
                menuWidth = 48.dp,
                onSelected = { onChangePeriod(it) },
            )

            MovingAveragesComponent(
                //modifier = Modifier.align(Alignment.Start),
                movingAverage1 = state.movingAverage1,
                movingAverage2 = state.movingAverage2,
                onChangeMovingAverages = onChangeMovingAverages
            )

            Text(stringResource(R.string.home_legend_start))
            Text("${state.startWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr")

            HorizontalDivider(modifier = Modifier.width(70.dp))

            Text(stringResource(R.string.home_legend_current))
            Text(
                "${state.currentWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr",
                fontWeight = FontWeight.Bold,
                color = Color.Blue,
            )

            HorizontalDivider(modifier = Modifier.width(70.dp))

            Text(stringResource(R.string.home_legend_target))
            Text("${state.destinationWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr")

            HorizontalDivider(modifier = Modifier.width(70.dp))

            Text(stringResource(R.string.home_legend_change, periodStr))
            Text("${state.periodWeightChange?.let { "%+.1f".format(it) } ?: "–"} $unitStr")

            HorizontalDivider(modifier = Modifier.width(70.dp))

            Text(stringResource(R.string.home_legend_to_target))
            Text("${state.toDestinationWeight?.let { "%.1f".format(it) } ?: "–"} $unitStr")
        }
    }
}


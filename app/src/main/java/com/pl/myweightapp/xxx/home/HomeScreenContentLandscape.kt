package com.pl.myweightapp.xxx.home

import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
                ChartImageContentVertical(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp),
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                val transition = rememberInfiniteTransition()
                val progress by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 6000),
                    )
                )
                Text(
                    stringResource(R.string.home_processing_please_be_patient),
                    fontSize = 14.sp,
                    modifier = Modifier.offset(y = 24.dp)
                )
                //val animatedProgress by animateFloatAsState(targetValue = state.progress)
                LinearProgressIndicator(
                    //progress = { state.progress },
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
//                        .height(4.dp)
                )
            }
        }
    }
}


@Composable
fun ChartImageContentVertical(
    modifier: Modifier = Modifier,
    state: UiState,
    //onChangePeriod: (DisplayPeriod) -> Unit,
    //onChangeMovingAverages: (Int?, Int?) -> Unit,
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
                    Log.d(TAG, "Chart size from box: $size")
                    if (chartSize.width > 0 && chartSize.height > 0) {
                        onChangeChartDimensions(chartSize.width, chartSize.height)
                    }
                }
            }
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                //.onSizeChanged { sz -> Log.d(TAG,"new size:box:$sz") }
                //.border(width = Dp.Hairline, color = Color.Magenta)
                //.clipToBounds()
                .horizontalScroll(scrollState),
            contentAlignment = Alignment.CenterStart
        ) {
            //TODO - uwzględnić state.useEmbeddedChart
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

            MovingAveragesComponentLandscape(
                modifier = Modifier.align(Alignment.Start),
                state = state,
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

@Composable
fun MovingAveragesComponentLandscape(
    modifier: Modifier = Modifier,
    state: UiState,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
) {

    var showMaPopup by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (showMaPopup) 180f else 0f)
    val density = LocalDensity.current
    val offsetY = with(density) { (-0).dp.roundToPx() }
    Box() {
        Button(
            onClick = { showMaPopup = !showMaPopup },
            modifier = modifier, //.height(24.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                //horizontal = 4.dp,
                //vertical = 0.dp
            )
        ) {
            Text(
                "MA: ${state.movingAverage1 ?: "–"} / ${state.movingAverage2 ?: "–"}",
                //style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(0.dp))
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
}
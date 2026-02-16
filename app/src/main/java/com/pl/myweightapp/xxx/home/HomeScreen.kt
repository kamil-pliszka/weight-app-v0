package com.pl.myweightapp.xxx.home

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.myweightapp.R
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents
import com.pl.myweightapp.persistence.DisplayPeriod
import com.pl.myweightapp.xxx.EnumDropdownButton


@Preview(name = "processing")
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        state = UiState(isProcessing = true),
        //onRefresh = { },
        onChangePeriod = { },
        onChangeMovingAverages = { _, _ -> },
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
        println("got event: $event")
        when (event) {
            is UiEvent.Error -> snackbarHostState.showSnackbar(
                messageErrorPrefix + event.message,
                duration = SnackbarDuration.Long
            )

            is UiEvent.Info -> snackbarHostState.showSnackbar(event.message)
        }
    }

    HomeScreenContent(
        modifier = modifier,
        state = state,
        //onRefresh = { viewModel.onAction(Action.OnRefreshChartAction) },
        onChangePeriod = { viewModel.onAction(Action.OnChangePeriod(it)) },
        onChangeMovingAverages = { ma1, ma2 ->
            viewModel.onAction(
                Action.OnChangeMovingAverages(
                    ma1,
                    ma2
                )
            )
        },
    )
}


@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: UiState,
    //onRefresh: () -> Unit,
    onChangePeriod: (DisplayPeriod) -> Unit,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
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
                ChartImageContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),   // 👈 zajmuje całą pozostałą przestrzeń
                    state = state,
                    //onClickGenerate = onRefresh,
                    onChangePeriod = onChangePeriod,
                    onChangeMovingAverages = onChangeMovingAverages,
                )
                LegendBottom(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    state = state
                )
            }
        }

        if (state.isProcessing) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
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
fun ChartImageContent(
    modifier: Modifier = Modifier,
    state: UiState,
    //onClickGenerate: () -> Unit,
    onChangePeriod: (DisplayPeriod) -> Unit,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
) {
    // val painter = rememberAsyncImagePainter("file:///android_asset/my_chart.png") // Jeśli w assets
    //val painter =
    //    if (state.chartBitmap != null) BitmapPainter(state.chartBitmap) else painterResource(id = R.drawable.ic_chart_trend_down)

    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }
//    val density = LocalDensity.current
//    val px = with(density) { 4.dp.toPx() }
//    println("ChartImageContent.padding: 4dp = ${px}px")
    Box(
        modifier = modifier
            .padding(4.dp)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                //.onSizeChanged { sz -> println("new size:box:$sz") }
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
                        .fillMaxHeight()
                        .onSizeChanged { sz -> println("new size:img:$sz") },
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

        MovingAveragesComponent(
            modifier = Modifier.align(Alignment.BottomStart),
            state = state,
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
        Text("MA: ${state.profile?.movingAverage1 ?: "–"} / ${state.profile?.movingAverage2 ?: "–"}")
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.rotate(rotation)
        )
    }
    if (showMaPopup) {
        var draftMa1 by remember { mutableIntStateOf(state.profile?.movingAverage1 ?: 1) }
        var draftMa2 by remember { mutableIntStateOf(state.profile?.movingAverage2 ?: 1) }

        Popup(
            alignment = Alignment.BottomStart,
            offset = IntOffset(0, offsetY),
            onDismissRequest = { showMaPopup = false },
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
                            onClick = { showMaPopup = false }
                        ) {
                            Text(stringResource(R.string.home_ma_cancel))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onChangeMovingAverages(draftMa1, draftMa2)
                                showMaPopup = false
                            }
                        ) {
                            Text(stringResource(R.string.home_ma_apply))
                        }
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
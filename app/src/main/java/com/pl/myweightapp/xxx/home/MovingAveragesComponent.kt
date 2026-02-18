package com.pl.myweightapp.xxx.home

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pl.myweightapp.R


/*
@Composable
fun MovingAveragesComponent(
    modifier: Modifier = Modifier,
    movingAverage1: Int?,
    movingAverage2: Int?,
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
        Text("MA: ${movingAverage1 ?: "–"} / ${movingAverage2 ?: "–"}")
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.rotate(rotation)
        )
    }

    if (showMaPopup) {
        MovingAveragesPopUp(
            movingAverage1,
            movingAverage2,
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
 */

@Composable
fun MovingAveragesComponent(
    modifier: Modifier = Modifier,
    movingAverage1: Int?,
    movingAverage2: Int?,
    onChangeMovingAverages: (Int?, Int?) -> Unit,
) {

    var showMaPopup by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (showMaPopup) 180f else 0f)
    Box(
        modifier = modifier, //.height(24.dp),
    ) {
        Button(
            onClick = { showMaPopup = !showMaPopup },
            //modifier = modifier, //.height(24.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                //horizontal = 4.dp,
                //vertical = 0.dp
            )
        ) {
            Text(
                "MA: ${movingAverage1 ?: "–"} / ${movingAverage2 ?: "–"}",
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
                movingAverage1,
                movingAverage2,
                onDismissRequest = { showMaPopup = false },
                onApply = { draftMa1, draftMa2 ->
                    onChangeMovingAverages(draftMa1, draftMa2)
                    showMaPopup = false
                }
            )
        }
    }
}

@Composable
fun MovingAveragesPopUp(
    movingAverage1: Int?,
    movingAverage2: Int?,
    onDismissRequest: () -> Unit,
    onApply: (Int?, Int?) -> Unit,
) {
    var draftMa1 by remember { mutableIntStateOf(movingAverage1 ?: 1) }
    var draftMa2 by remember { mutableIntStateOf(movingAverage2 ?: 1) }
    val popupAlignment = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
        Alignment.BottomEnd
    else
        Alignment.BottomStart
    val offsetDp = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
        DpOffset((-106).dp, 0.dp)
    else
        DpOffset(0.dp, (-48).dp)
    val density = LocalDensity.current
    //offsetYDp.roundToPx()
    val offsetInt = with(density) {
        IntOffset(
            x = offsetDp.x.roundToPx(),
            y = offsetDp.y.roundToPx()
        )
    }

    Popup(
        alignment = popupAlignment,
        offset = offsetInt,
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
                //.fillMaxWidth()
                .widthIn(max = 420.dp)      // ← zamiast .fillMaxWidth()
            //.padding(16.dp)
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

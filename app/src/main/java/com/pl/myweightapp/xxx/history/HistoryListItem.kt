package com.pl.myweightapp.xxx.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pl.myweightapp.persistence.WeightMeasureEntity
import com.pl.myweightapp.persistence.WeightUnit
import com.pl.myweightapp.ui.theme.MyWeightAppTheme
import com.pl.myweightapp.xxx.WieghtMeasureUi
import com.pl.myweightapp.xxx.toResourceId
import com.pl.myweightapp.xxx.toWeightMeasureUi
import com.pl.myweightapp.xxx.weightChangeColor
import java.time.Instant

@Composable
fun HistoryListItem(
    modifier: Modifier = Modifier,
    itemUi: WieghtMeasureUi,
    onClick: () -> Unit,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ).padding(vertical = 8.dp, horizontal = 4.dp)
            ,
        verticalAlignment = Alignment.CenterVertically,
        //horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = itemUi.date.formatted,
            fontSize = 14.sp,
            //fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        val unitLabel = stringResource(itemUi.unit.toResourceId())
        Text(
            text = "${itemUi.weight.formatted} $unitLabel",
            fontSize = 14.sp,
            //fontWeight = FontWeight.Light,
            color = contentColor,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(1f),
        )

        val changeColor = itemUi.change?.value.weightChangeColor()
        val changeText = itemUi.change?.formatted ?: ""
        Text(
            text = changeText,
            fontSize = 14.sp,
            //fontWeight = FontWeight.Bold,
            color = changeColor,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(0.6f).padding(end = 16.dp),
        )
        //Spacer(Modifier.width(32.dp))
    }
}

@PreviewLightDark
@Composable
private fun HistoryListItemPreview() {
    MyWeightAppTheme {
        HistoryListItem(
            itemUi = previewHistoryItem,
            onClick = { },
            modifier = Modifier.background(
                MaterialTheme.colorScheme.background
            )
        )
    }
}

internal val previewHistoryItem = WeightMeasureEntity(
    id = -123,
    date = Instant.now(),
    weight = "123.5".toBigDecimal(),
    unit = WeightUnit.KG,
).toWeightMeasureUi(change = "-0.3".toBigDecimal())
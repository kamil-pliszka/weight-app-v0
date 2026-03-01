package com.pl.myweightapp.feature.home.chart

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.chart.ChartImage

@Composable
fun ChartImageContent(
    modifier: Modifier = Modifier,
    chartImage: ChartImage?,
    scrollState: ScrollState
) {
    Box(
        modifier = modifier
            .horizontalScroll(scrollState),
        contentAlignment = Alignment.CenterStart
    ) {
        if (chartImage != null) {
            val bitmapImage = remember(chartImage) {
                decodeChartImageToBitmap(chartImage)
            }
            if (bitmapImage != null) {
                Image(
                    bitmap = bitmapImage,
                    contentDescription = stringResource(R.string.home_weight_graph),
                    modifier = Modifier
                        .fillMaxHeight(),
                    //.onSizeChanged { sz -> Log.d(TAG,"new size:img:$sz") }
                    contentScale = ContentScale.FillHeight
                )
            } else {
                Text(stringResource(R.string.chart_error_image))
            }
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
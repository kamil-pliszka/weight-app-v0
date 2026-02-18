package com.pl.myweightapp.xxx.home

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pl.myweightapp.R

@Composable
fun HomeScreenProgressBar() {
    val paddingBottomDp = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
        0.dp
    else
        80.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingBottomDp)
            //.padding(bottom = 80.dp)
        ,
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

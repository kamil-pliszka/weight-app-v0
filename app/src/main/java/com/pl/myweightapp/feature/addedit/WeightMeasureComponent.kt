package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pl.myweightapp.R
import com.pl.myweightapp.core.ui.InfiniteCircularList
import java.math.BigDecimal
import java.math.RoundingMode

private const val TAG = "WeightMeasureComponent"
@Composable
fun WeightMeasureComponent(
    modifier: Modifier = Modifier,
    initialMeasure: BigDecimal,
    textStyle: TextStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    textColor: Color = Color.LightGray,
    itemHeight: Dp = 40.dp,
    selectedTextColor: Color = Color.Black,
    onMeasureChanged: (BigDecimal) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
        //.padding(18.dp)
        //.border(1.dp, Color.Magenta),
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val integerPart = initialMeasure.setScale(0, RoundingMode.DOWN).toInt()
        val fractionalPart = initialMeasure.subtract(initialMeasure.setScale(0, RoundingMode.DOWN))
                .setScale(1, RoundingMode.HALF_UP)  // np. 0.456 → 0.5
                .movePointRight(1)                   // przesuwamy przecinek
                .toInt()
//        var currentInt by remember {
//            mutableStateOf(integerPart)
//        }
//        var currentFrac by remember {
//            mutableStateOf(fractionalPart)
//        }
        var currentInt = integerPart
        var currentFrac = fractionalPart

        InfiniteCircularList(
            width = 64.dp,
            itemHeight = itemHeight,
            items = (1..200).toList(),
            selectedItem = integerPart,
            textStyle = textStyle,
            textColor = textColor,
            selectedTextColor = selectedTextColor,
            contentAlignment = Alignment.CenterEnd,
            onItemSelected = { _, item ->
                Log.d(TAG,"Integer selected: $item, $currentFrac")
                currentInt = item
                onMeasureChanged(BigDecimal(currentInt) + BigDecimal(currentFrac).movePointLeft(1))
            }
            , xxxName = "integerPart"
            //,xxxAutoScrollable = true
        )
        //Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = " , ",
            style = textStyle,
        )
        //Spacer(modifier = Modifier.width(8.dp))
        InfiniteCircularList(
            width = 24.dp,
            itemHeight = itemHeight,
            items = (0..9).toList(),
            selectedItem = fractionalPart,
            textStyle = textStyle,
            textColor = textColor,
            selectedTextColor = selectedTextColor,
            contentAlignment = Alignment.CenterStart,
            onItemSelected = { _, item ->
                Log.d(TAG,"Fracction selected: $currentInt, $item, $currentFrac")
                if (item < 2 && currentFrac > 7) {
                    //moveUP
                    Log.d(TAG,"moveUP")
                    //currentInt = currentInt + 1
                } else if (item > 7 && currentFrac < 2) {
                    //moveDown
                    Log.d(TAG,"moveDown")
                    //currentInt = currentInt - 1
                }
                currentFrac = item
                onMeasureChanged(BigDecimal(currentInt) + BigDecimal(currentFrac).movePointLeft(1))
            }
            , xxxName = "fractionalPart"
        )
        //Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.weight_unit_kg),
            style = textStyle,
        )

    }
}


@Preview
@Composable
fun WeightMeasureComponentPreview() {
    WeightMeasureComponent(
        modifier = Modifier,
        initialMeasure = "99.4".toBigDecimal(),
        onMeasureChanged = {}
    )
}
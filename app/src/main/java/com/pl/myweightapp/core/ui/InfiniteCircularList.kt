package com.pl.myweightapp.core.ui

import android.util.Log
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//https://stackoverflow.com/questions/69734451/is-there-a-way-to-create-scroll-wheel-in-jetpack-compose
//https://gist.github.com/slaviboy/50e8d852f3e46543aad061c4141af87a

@Preview
@Composable
fun InfiniteCircularListPreview() {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        InfiniteCircularList(
            width = 100.dp,
            itemHeight = 70.dp,
            items = (0..9).toList(),
            selectedItem = 1,
            textStyle = TextStyle(fontSize = 23.sp),
            textColor = Color.LightGray,
            selectedTextColor = Color.Black,
            onItemSelected = { i, item ->
                Log.d("","Item selected: $i, $item")
            }
        )
    }
}

//@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> InfiniteCircularList(
    width: Dp,
    itemHeight: Dp,
    numberOfDisplayedItems: Int = 3,
    items: List<T>,
    selectedItem: T,
    itemScaleFact: Float = 1.5f,
    textStyle: TextStyle,
    textColor: Color,
    selectedTextColor: Color,
    contentAlignment: Alignment = Alignment.Center,
    onItemSelected: (index: Int, item: T) -> Unit = { _, _ -> },
    xxxName: String = "",
    xxxAutoScrollable: Boolean = false
) {
    //val itemHalfHeight = LocalDensity.current.run { itemHeight.toPx() / 2f }
    //val scrollState = rememberLazyListState(0)
    val scrollState = rememberSaveable(
        saver = LazyListState.Saver
    ) {
        LazyListState(0, 0)
    }
//    var lastSelectedIndex by remember {
//        mutableStateOf(0)
//    }
    var lastSelectedIndex by rememberSaveable {
        mutableStateOf(0)
    }
//    var itemsState by rememberSaveable {
//        mutableStateOf(items)
//    }
    //var initialized by rememberSaveable { mutableStateOf(false) }
    //Log.d(TAG,"recomposition $xxxName: sel = $selectedItem, init = $initialized")
    val key2 = if (xxxAutoScrollable) selectedItem else 0
    LaunchedEffect(items,  key2) {
        //Log.d(TAG,"LaunchedEffect $xxxName")
        var targetIndex = items.indexOf(selectedItem) - 1
        targetIndex += ((Int.MAX_VALUE / 2) / items.size) * items.size
        //itemsState = items
        lastSelectedIndex = targetIndex
        scrollState.scrollToItem(targetIndex)
        //scrollState.animateScrollToItem(targetIndex)
    }

    val density = LocalDensity.current
    val itemHalfHeightPx = with(density) { itemHeight.toPx() / 2f }

    LazyColumn(
        modifier = Modifier
            .width(width)
            .height(itemHeight * numberOfDisplayedItems),
        state = scrollState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollState)
    ) {
        items(
            count = Int.MAX_VALUE,
            key = { it },  // pomaga przy recomposition
            itemContent = { i ->
                //val item = itemsState[i % itemsState.size]
                val item = items[i % items.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeightPx
                            val parentHalfHeight =
                                (coordinates.parentCoordinates?.size?.height ?: 0) / 2f
                            val isSelected =
                                (y > parentHalfHeight - itemHalfHeightPx && y < parentHalfHeight + itemHalfHeightPx)
                            if (isSelected && lastSelectedIndex != i) {
                                //onItemSelected(i % itemsState.size, item)
                                onItemSelected(i % items.size, item)
                                lastSelectedIndex = i
                            }
                        },
                    contentAlignment = contentAlignment
                ) {
                    Text(
                        text = item.toString(),
                        style = textStyle,
                        color = if (lastSelectedIndex == i) {
                            selectedTextColor
                        } else {
                            textColor
                        },
                        fontSize = if (lastSelectedIndex == i) {
                            textStyle.fontSize * itemScaleFact
                        } else {
                            textStyle.fontSize
                        }
                    )
                }
            }
        )
    }
}

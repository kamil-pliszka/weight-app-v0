package com.pl.myweightapp.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> observeAsEvents(
    events: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: suspend (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    //LaunchedEffect(lifecycleOwner.lifecycle, key1, key2) {
    LaunchedEffect(events, key1, key2, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            //withContext(Dispatchers.Main.immediate) { - mozna bezpiecznie usunac
                events.collect(onEvent)
            //}
        }
    }
}
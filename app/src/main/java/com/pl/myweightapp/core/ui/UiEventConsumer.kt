package com.pl.myweightapp.core.ui

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.pl.myweightapp.core.presentation.UiEvent
import com.pl.myweightapp.core.presentation.asString
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.flow.Flow

@Composable
fun UiEventConsumer(
    snackbarHostState: SnackbarHostState,
    events: Flow<UiEvent>,
    key1: Any? = null,
    key2: Any? = null,
) {
    //val messageErrorPrefix = stringResource(R.string.error_msg_prefix)
    val context = LocalContext.current // <--- tutaj masz Context
    ObserveAsEvents(events, key1, key2) { event ->
        Log.d("ShowUiEventComponent", "got event: $event")
        when (event) {
            is UiEvent.Error -> {
                showErrorToast(context, event.asString(context))
            }

            is UiEvent.NetError -> snackbarHostState.showSnackbar(
                message = event.error.toString(),
                duration = SnackbarDuration.Long
            )

            is UiEvent.Info -> snackbarHostState.showSnackbar(event.asString(context))
            is UiEvent.Message -> snackbarHostState.showSnackbar(event.message)
        }
    }
}
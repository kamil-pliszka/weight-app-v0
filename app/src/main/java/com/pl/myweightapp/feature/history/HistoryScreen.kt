package com.pl.myweightapp.feature.history

import android.util.Log
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pl.myweightapp.R
import com.pl.myweightapp.core.ui.ConfirmationDialog
import kotlinx.coroutines.launch

private const val TAG = "HistoryScreen"
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    state: HistoryUiState,
    onAction: (HistoryAction) -> Unit,
) {
    //val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        if (state.measurements.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.history_no_measurements))
            }
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(HistoryAction.OnRefreshAction) }
        ) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.measurements, key = { it.id }) { itemUi ->
                    val scope = rememberCoroutineScope()

                    HistoryListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(itemUi.id) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    if (dragAmount > 40f) { // przesunięcie w prawo
                                        scope.launch {
                                            Log.d(TAG,"Invoke onDelete for ${itemUi.id} ..., dragAmount = $dragAmount")
                                            onAction(
                                                HistoryAction.OnItemDeleteAction(
                                                    itemUi
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                        itemUi = itemUi,
                        onClick = {
                            onAction(HistoryAction.OnItemEditAction(itemUi))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }


//    state.editingItemId?.let { itemId ->
//        //Log.d(TAG,"composable state.editingItemId is set")
//        val editVm: EditMeasureViewModel = viewModel(
//            key = "edit-$itemId",
//            factory = EditMeasureViewModelFactory(itemId, viewModel.repository)
//        )
//        EditMeasureDialog(
//            modifier = modifier,
//            onDismiss = {
//                viewModel.onAction(HistoryAction.OnCloseEditAction, navController)
//            },
//            snackbarHostState = snackbarHostState,
//            viewModel = editVm
//        )
//    }

    state.deletingItem?.let { itemUi ->
        ConfirmationDialog(
            title = stringResource(R.string.history_delete_measurement),
            text = stringResource(
                R.string.history_are_you_sure_you_want_to_delete_measurement,
                itemUi.date.formatted
            ),
            onConfirm = {
                onAction(HistoryAction.OnConfirmDeleteAction(itemUi.id))
            },
            confirmText = stringResource(R.string.history_delete_button),
            confirmColor = MaterialTheme.colorScheme.error,
            onCancel = {
                onAction(HistoryAction.OnCancelDeleteAction)
            },
        )
    }
}
package com.pl.myweightapp.xxx.history

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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.myweightapp.R
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents
import com.pl.myweightapp.xxx.ConfirmationDialog
import com.pl.myweightapp.xxx.add_edit.EditMeasureDialog
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    viewModel: HistoryViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                Text("Brak pomiarów historycznych")
            }
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onAction(HistoryAction.OnRefreshAction) }
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
                                            println("Invoke onDelete for ${itemUi.id} ..., dragAmount = $dragAmount")
                                            viewModel.onAction(
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
                            viewModel.onAction(HistoryAction.OnItemEditAction(itemUi))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    state.editingItemId?.let { itemId ->
        //println("composable state.editingItemId is set")
//        val editVm: EditMeasureViewModel = viewModel(key = "edit-$itemId") {
//            EditMeasureViewModel(itemId)
//        }
        EditMeasureDialog(
            modifier = modifier,
            itemId = itemId,
            onDismiss = {
                viewModel.onAction(HistoryAction.OnCloseEditAction)
            },
            snackbarHostState = snackbarHostState,
            scope = scope,
            //viewModel = editVm
        )
    }

    state.deletingItem?.let { itemUi ->
        ConfirmationDialog(
            title = "Delete measurement?",
            text = "Are you sure you want to delete measurement ${itemUi.date.formatted}?",
            onConfirm = {
                viewModel.onAction(HistoryAction.OnConfirmDeleteAction(itemUi.id))
            },
            confirmText = "Delete",
            confirmColor = MaterialTheme.colorScheme.error,
            onCancel = {
                viewModel.onAction(HistoryAction.OnCancelDeleteAction)
            },
        )
    }

    val messageSuccessfulyDeleted = stringResource(R.string.successfully_deleted)
    val messageErrorPrefix = stringResource(R.string.error_msg_prefix)
    ObserveAsEvents(viewModel.events) { event ->
        println("got output event: $event")
        scope.launch {
            val msg = when (event) {
                UiEvent.Deleted -> messageSuccessfulyDeleted
                is UiEvent.Error -> messageErrorPrefix + event.message
            }
            snackbarHostState.showSnackbar(msg)
        }
    }

}
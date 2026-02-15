package com.pl.myweightapp.xxx.add_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.myweightapp.R
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents
import com.pl.myweightapp.xxx.ConfirmationDialog
import com.pl.myweightapp.xxx.toDateString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EditMeasureDialog(
    modifier: Modifier = Modifier,
    itemId: Long,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    viewModel: EditMeasureViewModel = viewModel(key = "edit-$itemId") {
        EditMeasureViewModel(itemId)
    }
    //viewModel: EditMeasureViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        EditMeasureUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EditMeasureUiState.Loaded -> {
            val state = state as EditMeasureUiState.Loaded
            AlertDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        //.border(1.dp, Color.Cyan)
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Cancel"
                            )
                        }
                        TextButton(onClick = viewModel::onDeleteAction) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = "Delete"
                            )
                        }
                        TextButton(onClick = viewModel::onSaveAction) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "OK"
                            )
                        }
                    }
                },
                //dismissButton = {},
                title = {
                    Column {
                        Text("Edytuj pomiar: ${state.date.toDateString()}")
                        HorizontalDivider()
                    }
                },
                text = {
                    WeightMeasureComponent(
                        initialMeasure = state.weight,
                        onMeasureChanged = viewModel::updateMeasure
                    )
                }
            )

            if (state.showDeleteConfirm) {
                ConfirmationDialog(
                    title = "Delete measurement?",
                    text = "Are you sure you want to delete this measurement?",
                    onConfirm = viewModel::onConfirmDelete,
                    confirmText = "Delete",
                    confirmColor = MaterialTheme.colorScheme.error,
                    onCancel = viewModel::onCancelDelete,
                )
            }
        }

        EditMeasureUiState.Saving -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        EditMeasureUiState.Deleted -> {
            val message = stringResource(R.string.successfully_deleted)
            val savedState = state
            LaunchedEffect(savedState) {
                snackbarHostState.showSnackbar(message)
                onDismiss() // zamykamy dialog
            }
        }

        /*
        EditMeasureUiState.Saved -> {
            val message = stringResource(R.string.successfuly_saved)
            val savedState = state
            LaunchedEffect(savedState) {
                snackbarHostState.showSnackbar(message)
                onDismiss() // zamykamy dialog
            }
        }

        is EditMeasureUiState.Error -> {
            LaunchedEffect(state) {
                if (state is EditMeasureUiState.Error) {
                    val message = (state as EditMeasureUiState.Error).message
                    snackbarHostState.showSnackbar(message)
                    onDismiss() // zamykamy dialog
                }
            }
        }
        */
    }

    val messageSuccessfulySaved = stringResource(R.string.successfully_saved)
    val messageSuccessfulyDeleted = stringResource(R.string.successfully_deleted)
    val messageErrorPrefix = stringResource(R.string.error_msg_prefix)
    ObserveAsEvents(viewModel.events) { event ->
        println("got output event: $event")
        scope.launch {
            val msg = when (event) {
                UiEvent.Deleted -> messageSuccessfulyDeleted
                UiEvent.Saved -> messageSuccessfulySaved
                is UiEvent.Error -> messageErrorPrefix + event.message
            }
            snackbarHostState.showSnackbar(msg)
        }
        onDismiss()
    }

}

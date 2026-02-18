package com.pl.myweightapp.feature.addedit

import android.util.Log
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
import com.pl.myweightapp.core.ui.ConfirmationDialog
import com.pl.myweightapp.core.util.toDateString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "EditMeasureDialog"
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
                                contentDescription = stringResource(R.string.edit_measure_cancel)
                            )
                        }
                        TextButton(onClick = viewModel::onDeleteAction) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = stringResource(R.string.edit_measure_delete)
                            )
                        }
                        TextButton(onClick = viewModel::onSaveAction) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = stringResource(R.string.edit_measure_ok)
                            )
                        }
                    }
                },
                //dismissButton = {},
                title = {
                    Column {
                        Text(stringResource(R.string.edit_measure_title, state.date.toDateString()))
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
                    title = stringResource(R.string.edit_measure_delete_measurement),
                    text = stringResource(R.string.edit_measure_are_you_sure_you_want_to_delete_this_measurement),
                    onConfirm = viewModel::onConfirmDelete,
                    confirmText = stringResource(R.string.edit_measure_delete_button),
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
        Log.d(TAG,"got output event: $event")
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

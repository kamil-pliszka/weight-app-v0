package com.pl.myweightapp.feature.addedit

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.pl.myweightapp.R
import com.pl.myweightapp.core.util.toDateString
import com.pl.myweightapp.feature.common.ui.ConfirmationDialog
import com.pl.myweightapp.feature.common.ui.toWeightUnit

@Composable
fun EditMeasureDialog(
    state: EditMeasureUiState,
    onAction: (EditAction) -> Unit,
    //onDismiss: () -> Unit,
) {
    //val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        EditMeasureUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                //CircularProgressIndicator()
            }
        }

        is EditMeasureUiState.Loaded -> {
            //val state = state as EditMeasureUiState.Loaded
            AlertDialog(
                onDismissRequest = { onAction(EditAction.OnDismissAction) },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        //.border(1.dp, Color.Cyan)
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = { onAction(EditAction.OnDismissAction) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = stringResource(R.string.edit_measure_cancel)
                            )
                        }
                        TextButton(onClick = { onAction(EditAction.OnDeleteAction) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = stringResource(R.string.edit_measure_delete)
                            )
                        }
                        TextButton(onClick = { onAction(EditAction.OnSaveAction) }) {
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
                        weightUnit = state.unit.toWeightUnit(),
                        onMeasureChanged = { onAction(EditAction.OnUpdateMeasure(it)) },
                        onToggleWeightUnit = {onAction(EditAction.ToggleWeightUnit) },
                    )
                }
            )

            if (state.showDeleteConfirm) {
                ConfirmationDialog(
                    title = stringResource(R.string.edit_measure_delete_measurement),
                    text = stringResource(R.string.edit_measure_are_you_sure_you_want_to_delete_this_measurement),
                    onConfirm = { onAction(EditAction.OnConfirmDelete) },
                    confirmText = stringResource(R.string.edit_measure_delete_button),
                    confirmColor = MaterialTheme.colorScheme.error,
                    onCancel = { onAction(EditAction.OnCancelDelete) },
                )
            }
        }

        EditMeasureUiState.Processing -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

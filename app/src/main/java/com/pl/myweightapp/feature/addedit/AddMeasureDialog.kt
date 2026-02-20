package com.pl.myweightapp.feature.addedit

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pl.myweightapp.R
import com.pl.myweightapp.core.ui.UiEventConsumer
import com.pl.myweightapp.core.util.toDateString
import com.pl.myweightapp.core.util.toMillis
import java.time.LocalDate


@Composable
fun AddMeasureDialog(
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: AddMeasureViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    println("AddMeasureDialog: ${state}")

    UiEventConsumer(
        events = viewModel.events,
        snackbarHostState = snackbarHostState
    )

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //CircularProgressIndicator()
        }
    } else {
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
                        //Text("ANULUJ")
                        Icon(
                            imageVector = Icons.Default.Close,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(R.string.add_measure_cancel)
                        )
                    }
                    TextButton(onClick = viewModel::onShowDateDialogAction) {
                        Text(state.choosenDate.toDateString())
                    }
                    TextButton(onClick = {
                        viewModel.onDialogConfirmAction()
                        onDismiss()
                    }) {
                        //Text("OK")
                        Icon(
                            imageVector = Icons.Default.Check,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(R.string.add_measure_ok)
                        )
                    }
                }
            },
            //dismissButton = {},
            title = {
                Column {
                    Text(stringResource(R.string.add_measure_title))
                    HorizontalDivider()
                }
            },
            text = {
                WeightMeasureComponent(
                    initialMeasure = state.currentWeightMeasure,
                    onMeasureChanged = viewModel::updateCurrentMeasure
                )
            },
        )
    }

    if (state.showDateDialog) {
        val maxDate = remember {
            Log.d("AddMeasureDialog","Compute maxDate in remember")
            LocalDate.now().toMillis()
        }
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= maxDate
                }
            },
            initialSelectedDateMillis = state.choosenDate.toMillis()
        )
        LocalDatePickerDialog(
            datePickerState = datePickerState,
            onDismissRequest = { viewModel.onCloseDateDialog() },
            onDatePicked = { newDate ->
                newDate?.let {
                    viewModel.updateChoosenDate(it)
                }
                viewModel.onCloseDateDialog()
            },
        )
    }
}



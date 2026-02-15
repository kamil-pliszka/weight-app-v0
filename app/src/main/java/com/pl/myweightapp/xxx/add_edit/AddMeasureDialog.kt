package com.pl.myweightapp.xxx.add_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.myweightapp.xxx.toDateString
import com.pl.myweightapp.xxx.toMillis
import java.time.LocalDate


@Composable
fun AddMeasureDialog(
    viewModel: AddMeasureViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDialogDismissAction,
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    //.border(1.dp, Color.Cyan)
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = viewModel::onDialogDismissAction) {
                        //Text("ANULUJ")
                        Icon(
                            imageVector = Icons.Default.Close,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Cancel"
                        )
                    }
                    TextButton(onClick = viewModel::onShowDateDialogAction) {
                        Text(state.choosenDate.toDateString())
                    }
                    TextButton(onClick = viewModel::onDialogConfirmAction) {
                        //Text("OK")
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
                    Text("Dodaj pomiar")
                    HorizontalDivider()
                }
            },
            text = {
                WeightMeasureComponent(
                    initialMeasure = state.currentWeightMeasure,
                    onMeasureChanged = viewModel::updateCurrentMeasure
                )
            }
        )
    }

    if (state.showDateDialog) {
        val maxDate = remember {
            println("Compute maxDate in remember")
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



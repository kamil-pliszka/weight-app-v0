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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pl.myweightapp.R
import com.pl.myweightapp.core.domain.WeightUnit
import com.pl.myweightapp.core.util.toDateString
import com.pl.myweightapp.core.util.toMillis
import java.math.BigDecimal
import java.time.LocalDate


@Preview(name = "processing")
@Composable
fun AddMeasureDialogPreview() {
    AddMeasureDialog(
        state = AddMeasureState(
            lastWeight = BigDecimal("123.4"),
            weightUnit = WeightUnit.LB,
        ),
        onAction = {},
//        onChangePeriod = { },
//        onChangeMovingAverages = { _, _ -> },
//        onChangeChartDimensions = { _, _ -> },
    )
}

@Composable
fun AddMeasureDialog(
    state: AddMeasureState,
    onAction: (AddAction) -> Unit,
    //onDismiss: () -> Unit,
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //CircularProgressIndicator()
        }
    } else {
        AlertDialog(
            onDismissRequest = { onAction(AddAction.OnDismissAction) },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    //.border(1.dp, Color.Cyan)
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { onAction(AddAction.OnDismissAction) }) {
                        //Text("ANULUJ")
                        Icon(
                            imageVector = Icons.Default.Close,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(R.string.add_measure_cancel)
                        )
                    }
                    TextButton(onClick = { onAction(AddAction.OnShowDateDialogAction) }) {
                        Text(state.choosenDate.toDateString())
                    }
                    TextButton(onClick = { onAction(AddAction.OnDialogConfirmAction) }) {
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
                    weightUnit = state.weightUnit,
                    onMeasureChanged = { onAction(AddAction.UpdateCurrentMeasure(it)) },
                    onToggleWeightUnit = { onAction(AddAction.ToggleWeightUnit) },
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
            onDismissRequest = { onAction(AddAction.OnCloseDateDialog) },
            onDatePicked = { newDate ->
                newDate?.let {
                    onAction(AddAction.UpdateChoosenDate(it))
                }
                onAction(AddAction.OnCloseDateDialog)
            },
        )
    }
}



package com.pl.myweightapp.xxx.add_edit

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pl.myweightapp.xxx.millisToLocalDate
import java.time.LocalDate

@Composable
fun LocalDatePickerDialog(
    datePickerState: DatePickerState,
    onDismissRequest: () -> Unit,
    onDatePicked: (LocalDate?) -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onDatePicked(datePickerState.selectedDateMillis?.millisToLocalDate())
                }
            ) {
                //Text("OK", color = MaterialTheme.colorScheme.primary)
                Icon(
                    imageVector = Icons.Default.Check,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "OK"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                //Text("Cancel", color = MaterialTheme.colorScheme.primary)
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Cancel"
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            // Make DatePicker smaller
            modifier = Modifier
                .sizeIn(maxWidth = 350.dp)
                .padding(8.dp)
        )
    }
}

package com.pl.myweightapp.xxx

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ConfirmationDialog(
    title: String = "Confirmation",
    text: String = "Are you sure?",
    onConfirm: () -> Unit,
    //confirmContent: @Composable RowScope.() -> Unit = { Text("OK") },
    confirmText : String = "OK",
    confirmColor: Color = Color.Unspecified,
    onCancel: () -> Unit,
    cancelText : String = "Cancel",
    cancelColor: Color = Color.Unspecified,
) {

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(title)
        },
        text = {
            Text(text)
        },

        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(confirmText, color = confirmColor)
            }
        },

        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(cancelText, color = cancelColor)
            }
        }
    )
}
package com.pl.myweightapp.xxx

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.pl.myweightapp.R

@Composable
fun ConfirmationDialog(
    title: String = stringResource(R.string.confirmation_title),
    text: String = stringResource(R.string.confirmation_are_you_sure),
    onConfirm: () -> Unit,
    //confirmContent: @Composable RowScope.() -> Unit = { Text("OK") },
    confirmText : String = stringResource(R.string.confirmation_confitm_text),
    confirmColor: Color = Color.Unspecified,
    onCancel: () -> Unit,
    cancelText : String = stringResource(R.string.confirmation_cancel_text),
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
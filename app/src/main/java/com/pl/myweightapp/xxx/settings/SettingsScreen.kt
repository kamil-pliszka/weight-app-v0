package com.pl.myweightapp.xxx.settings

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.myweightapp.R
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents
import com.pl.myweightapp.xxx.ConfirmationDialog

private const val TAG = "SettingsScreen"

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    viewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val launcherChooseFileImport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        Log.d(TAG, "onResult, uri: $uri")
        if (uri != null) {
            viewModel.onAction(Action.OnCsvImport(uri))
        }
    }

    val launcherChooseFileExport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        Log.d(TAG, "onResult, uri: $uri")
        if (uri != null) {
            viewModel.onAction(Action.OnCsvExport(uri))
        }
    }

    val messageErrorPrefix = stringResource(R.string.error_msg_prefix)
    ObserveAsEvents(viewModel.events) { event ->
        Log.d(TAG, "got event: $event")
        when (event) {
            is UiEvent.Error -> snackbarHostState.showSnackbar(
                messageErrorPrefix + event.message,
                duration = SnackbarDuration.Long
            )

            is UiEvent.Info -> snackbarHostState.showSnackbar(event.message)
        }
    }

    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_profile)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
            ProfileComponent(
                modifier = Modifier.fillMaxWidth(),
                snackbarHostState = snackbarHostState
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_general)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
            DataRow(Modifier.clickable {
                viewModel.onAction(Action.OnLanguageClick)
            }) {
                Text(stringResource(R.string.settings_language))
                Spacer(Modifier.width(24.dp))
                Text(state.langDisplayResId?.let { stringResource(it) } ?: "–")
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_backup_and_recovery)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )

            DataRow(Modifier.clickable {
                launcherChooseFileExport.launch(viewModel.suggestedExportFileName())
            }) {
                Text(stringResource(R.string.settings_csv_export))
            }
            HorizontalDivider()
            DataRow(Modifier.clickable {
                launcherChooseFileImport.launch(
                    arrayOf("text/csv", "application/csv", "text/comma-separated-values")
                )
            }) {
                Text(stringResource(R.string.settings_csv_import))
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_reset)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
            DataRow(Modifier.clickable {
                viewModel.onAction(Action.OnDeleteAllDataClick)
            }) {
                Text(
                    stringResource(R.string.settings_delete_all_data),
                    color = MaterialTheme.colorScheme.error
                )
            }

        }
    }
    if (state.isCsvProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.settings_processing_csv_please_be_patient),
                modifier = Modifier.offset(y = 64.dp)
            )
            val animatedProgress by animateFloatAsState(targetValue = state.csvProgress)
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
//            CircularProgressIndicator(
//                progress = { animatedProgress },
//                strokeWidth = 8.dp,
//                modifier = Modifier.size(80.dp)
//            )
        }
    }
    if (state.showDeleteConfirm) {
        ConfirmationDialog(
            title = stringResource(R.string.settings_delete_all_data_question),
            text = stringResource(R.string.settings_delete_all_data_description),
            onConfirm = {
                viewModel.onAction(Action.OnDeleteAllDataConfirm)
            },
            confirmText = stringResource(R.string.settings_delete_all_data_delete_button),
            confirmColor = MaterialTheme.colorScheme.error,
            onCancel = {
                viewModel.onAction(Action.OnDeleteAllDataCancel)
            },
        )
    }

    if (state.showLanguageChooser) {
        ChooseLangComponent(
            modifier = Modifier,
            onDismissRequest = { viewModel.onAction(Action.OnLanguageDismiss) },
            onChooseLang = { viewModel.onAction(Action.OnLanguageChoose(it)) }
        )
    }

}

@Composable
private inline fun DataRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChooseLangComponent(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onChooseLang: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.choose_lang),
                style = MaterialTheme.typography.titleLarge
            )

            LanguageItem(stringResource(R.string.lang_pl)) {
                onChooseLang("pl")
            }
            LanguageItem(stringResource(R.string.lang_en)) {
                onChooseLang("en")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguageItem(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
    }
}
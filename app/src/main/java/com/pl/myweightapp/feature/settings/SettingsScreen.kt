package com.pl.myweightapp.feature.settings

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pl.myweightapp.R
import com.pl.myweightapp.core.ui.ConfirmationDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "SettingsScreen"

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: UiState,
    onAction: (Action) -> Unit,
    profileState: ProfileUiState,
    profileOnAction: (ProfileAction) -> Unit,
    ) {
    //val state by viewModel.state.collectAsStateWithLifecycle()

    val launcherChooseFileImport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        Log.d(TAG, "onResult, uri: $uri")
        if (uri != null) {
            onAction(Action.OnCsvImport(uri))
        }
    }

    val launcherChooseFileExport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        Log.d(TAG, "onResult, uri: $uri")
        if (uri != null) {
            onAction(Action.OnCsvExport(uri))
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
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
                state = profileState,
                onAction = profileOnAction
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_general)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
            DataRow(Modifier.clickable {
                onAction(Action.OnLanguageClick)
            }) {
                Text(modifier = Modifier.weight(1f), text = stringResource(R.string.settings_language))
                Text(modifier = Modifier.weight(1f), text = state.langDisplayResId?.let { stringResource(it) } ?: "–")
            }
            DataRow {
                Text(modifier = Modifier.weight(1f), text = stringResource(R.string.settings_dynamic_chart))
//                Switch(
//                    checked = state.useEmbeddedChart,
//                    onCheckedChange = { checked ->
//                        viewModel.onAction(
//                            Action.OnChangeUseEmbeddedChart(checked)
//                        )
//                    }
//                )
                Checkbox(modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.Start),
                    checked = state.useEmbeddedChart,
                    onCheckedChange = { checked ->
                        onAction(
                            Action.OnChangeUseEmbeddedChart(checked)
                        )
                    }
                )
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_backup_and_recovery)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )

            DataRow(Modifier.clickable {
                launcherChooseFileExport.launch(suggestedExportFileName())
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

            if (state.visibleRestore) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_reset_backup)) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
                DataRow(Modifier.clickable {
                    onAction(Action.OnTryToRestore)
                }) {
                    Text(
                        stringResource(R.string.settings_try_to_restore),
                    )
                }
            } else {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_reset)) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            DataRow(Modifier.clickable {
                onAction(Action.OnDeleteAllDataClick)
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
                onAction(Action.OnDeleteAllDataConfirm)
            },
            confirmText = stringResource(R.string.settings_delete_all_data_delete_button),
            confirmColor = MaterialTheme.colorScheme.error,
            onCancel = {
                onAction(Action.OnDeleteAllDataCancel)
            },
        )
    }

    if (state.showLanguageChooser) {
        ChooseLangComponent(
            modifier = Modifier,
            onDismissRequest = { onAction(Action.OnLanguageDismiss) },
            onChooseLang = { onAction(Action.OnLanguageChoose(it)) }
        )
    }

}

fun suggestedExportFileName(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    val dateStr = LocalDateTime.now().format(formatter)
    return "MyWeight_$dateStr.csv"
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
        verticalAlignment = Alignment.CenterVertically,
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
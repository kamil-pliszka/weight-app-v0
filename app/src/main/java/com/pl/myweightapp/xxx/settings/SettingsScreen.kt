package com.pl.myweightapp.xxx.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
        println("onResult, uri: $uri")
        if (uri != null) {
            viewModel.onAction(Action.OnCsvImport(uri))
        }
    }

    val launcherChooseFileExport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        println("onResult, uri: $uri")
        if (uri != null) {
            viewModel.onAction(Action.OnCsvExport(uri))
        }
    }

    val messageErrorPrefix = stringResource(R.string.error_msg_prefix)
    ObserveAsEvents(viewModel.events) { event ->
        println("got event: $event")
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
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally
        ) {
            ListItem(
                headlineContent = { Text("Ogólne") },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
            /*DataColumn {
                Text(text = "Ogolne dane wiek")
                Text(text = "Ogolne dane wzrost")
                Text(text = "Ogolne dane waga docelowa")
            }*/
            ProfileComponent(
                modifier = Modifier.fillMaxWidth(),
                snackbarHostState = snackbarHostState
            )

            ListItem(
                headlineContent = { Text("Kopia zapasowa i przywracanie") },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )

            DataRow(Modifier.clickable {
                launcherChooseFileExport.launch(viewModel.suggestedExportFileName())
            }) {
                Text("Eksport do CSV")
            }
            HorizontalDivider()
            DataRow(Modifier.clickable {
                launcherChooseFileImport.launch(
                    arrayOf("text/csv", "application/csv", "text/comma-separated-values")
                )
            }) {
                Text("Import z CSV")
            }

            ListItem(
                headlineContent = { Text("Reset") },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
            DataRow(Modifier.clickable {
                viewModel.onAction(Action.OnDeleteAllDataClick)
            }) {
                Text("Usuń wszystkie dane", color = MaterialTheme.colorScheme.error)
            }

        }
    }
    if (state.isCsvProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Processing csv..., please be patient", modifier = Modifier.offset(y = 64.dp))
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
            title = "Delete all data?",
            text = "Are you sure you want to delete all data? This operation cannot be undone.",
            onConfirm = {
                viewModel.onAction(Action.OnDeleteAllDataConfirm)
            },
            confirmText = "Delete",
            confirmColor = MaterialTheme.colorScheme.error,
            onCancel = {
                viewModel.onAction(Action.OnDeleteAllDataCancel)
            },
        )
    }
}

@Composable
private inline fun DataColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        content()
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

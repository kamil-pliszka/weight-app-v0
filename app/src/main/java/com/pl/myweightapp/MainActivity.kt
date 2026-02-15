package com.pl.myweightapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents
import com.pl.myweightapp.navigation.BottomNavigationBar
import com.pl.myweightapp.navigation.Navigation
import com.pl.myweightapp.ui.theme.MyWeightAppTheme
import com.pl.myweightapp.xxx.add_edit.AddMeasureDialog
import com.pl.myweightapp.xxx.add_edit.AddMeasureViewModel
import com.pl.myweightapp.xxx.displayEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val snackBarState = remember { SnackbarHostState() }
            //val scope = rememberCoroutineScope()
            val navController = rememberNavController()

            val vmAddMeasure : AddMeasureViewModel = viewModel()
            val context = LocalContext.current
            ObserveAsEvents(events = vmAddMeasure.events) { event ->
                displayEvent(event, context)
            }

            MyWeightAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
//                                scope.launch {
//                                    snackBarState.showSnackbar(
//                                        message = "Clicked FAB"
//                                    )
//                                }
                                vmAddMeasure.onShowDialogAction()
                            },
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackBarState
                        )
                    },
                ) { innerPadding ->
                    Navigation(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        navController = navController,
                        snackbarHostState = snackBarState
                    )

                    AddMeasureDialog()
                }
            }
        }
    }
}
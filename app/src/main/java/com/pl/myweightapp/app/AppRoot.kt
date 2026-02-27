package com.pl.myweightapp.app

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pl.myweightapp.ui.theme.MyWeightAppTheme

@Composable
fun AppRoot() {
    val snackBarState = remember { SnackbarHostState() }
    //val scope = rememberCoroutineScope()
    val navController = rememberNavController()

//            val vmAddMeasure : AddMeasureViewModel = hiltViewModel()
//            UiEventConsumer(
//                events = vmAddMeasure.events,
//                snackbarHostState = snackBarState
//            )
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val navigationVM: NavigationViewModel = hiltViewModel() //by viewModels()

    MyWeightAppTheme {
        Scaffold(
            modifier = Modifier.Companion.fillMaxSize(),
            bottomBar = {
                if (!isLandscape) {
                    AppBottomNavigationBar(
                        navController = navController,
                        viewModel = navigationVM
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        //vmAddMeasure.onShowDialogAction()
                        navController.navigate(Screen.Add.route)
                    },
                    modifier = if (isLandscape) Modifier.Companion.offset(
                        x = 6.dp,
                        y = 8.dp
                    ) else Modifier.Companion,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            },
            floatingActionButtonPosition = if (isLandscape) FabPosition.Companion.End else FabPosition.Companion.Center,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackBarState
                )
            },
        ) { innerPadding ->
            if (isLandscape) {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    Box(Modifier.Companion.weight(1f)) {
                        Navigation(
                            navController = navController,
                            snackbarHostState = snackBarState
                        )
                    }
                    AppNavigationRail(
                        navController = navController,
                        viewModel = navigationVM
                    )
                }
            } else {
                Navigation(
                    modifier = Modifier.Companion
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    navController = navController,
                    snackbarHostState = snackBarState
                )
            }
            //AddMeasureDialog()
        }
    }
}
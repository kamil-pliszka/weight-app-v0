package com.pl.myweightapp.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pl.myweightapp.navigation.AppBottomNavigationBar
import com.pl.myweightapp.navigation.AppNavigationRail
import com.pl.myweightapp.navigation.Navigation
import com.pl.myweightapp.navigation.NavigationViewModel
import com.pl.myweightapp.navigation.Screen
import com.pl.myweightapp.ui.theme.MyWeightAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (!isLandscape) {
                            AppBottomNavigationBar(navController = navController, viewModel = navigationVM)
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                //vmAddMeasure.onShowDialogAction()
                                navController.navigate(Screen.Add.route)
                            },
                            modifier = if (isLandscape) Modifier.offset(x = 4.dp, y = 24.dp) else Modifier,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    },
                    floatingActionButtonPosition = if (isLandscape) FabPosition.End else FabPosition.Center,
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackBarState
                        )
                    },
                ) { innerPadding ->
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(Modifier.weight(1f).padding(innerPadding)) {
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
                            modifier = Modifier
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
    }
}

package com.pl.myweightapp.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.pl.myweightapp.core.presentation.util.ObserveAsEvents
import com.pl.myweightapp.core.ui.UiEventConsumer
import com.pl.myweightapp.feature.addedit.AddEditMeasureEvent
import com.pl.myweightapp.feature.addedit.AddMeasureDialog
import com.pl.myweightapp.feature.addedit.AddMeasureViewModel
import com.pl.myweightapp.feature.addedit.EditMeasureDialog
import com.pl.myweightapp.feature.addedit.EditMeasureViewModel
import com.pl.myweightapp.feature.history.HistoryScreen
import com.pl.myweightapp.feature.history.HistoryUiEvent
import com.pl.myweightapp.feature.history.HistoryViewModel
import com.pl.myweightapp.feature.home.HomeScreen
import com.pl.myweightapp.feature.home.HomeViewModel
import com.pl.myweightapp.feature.settings.ProfileViewModel
import com.pl.myweightapp.feature.settings.SettingsScreen
import com.pl.myweightapp.feature.settings.SettingsViewModel

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home")
    object HistoryScreen : Screen("history")
    object SettingsScreen : Screen("settings")
    object Add : Screen("add")
    object Edit : Screen("edit")
}

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(route = Screen.HomeScreen.route) {
            val viewModel = hiltViewModel<HomeViewModel>()
            UiEventConsumer(snackbarHostState, viewModel.events)
            val state by viewModel.state.collectAsStateWithLifecycle()
            HomeScreen(
                modifier = modifier,
                state = state,
                onAction = viewModel::onAction,
            )
        }
        composable(route = Screen.HistoryScreen.route) {
            val viewModel = hiltViewModel<HistoryViewModel>()
            UiEventConsumer(snackbarHostState, viewModel.events)
            ObserveAsEvents(viewModel.navEvents) { event ->
                when (event) {
                    is HistoryUiEvent.NavToEditMeasure -> {
                        navController.navigate("${Screen.Edit.route}/${event.id}")
                    }
                }
            }
            val state by viewModel.state.collectAsStateWithLifecycle()
            HistoryScreen(
                modifier = modifier,
                state = state,
                onAction = viewModel::onAction,
            )
        }
        dialog(
            route = Screen.Edit.route + "/{itemId}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.LongType }
            )
        ) {
            val viewModel = hiltViewModel<EditMeasureViewModel>()
            UiEventConsumer(snackbarHostState, viewModel.events)
            ObserveAsEvents(viewModel.navEvents) { event ->
                when (event) {
                    AddEditMeasureEvent.CloseDialog -> navController.popBackStack()
                }
            }
            val state by viewModel.state.collectAsStateWithLifecycle()
            EditMeasureDialog(
                state = state,
                onAction = viewModel::onAction,
            )
        }
        dialog(
            route = Screen.Add.route,
        ) {
            val viewModel = hiltViewModel<AddMeasureViewModel>()
            UiEventConsumer(snackbarHostState, viewModel.events)
            ObserveAsEvents(viewModel.navEvents) { event ->
                when (event) {
                    AddEditMeasureEvent.CloseDialog -> navController.popBackStack()
                }
            }
            val state by viewModel.state.collectAsStateWithLifecycle()
            AddMeasureDialog(
                state = state,
                onAction = viewModel::onAction,
            )
        }
        composable(route = Screen.SettingsScreen.route) {
            val viewModel = hiltViewModel<SettingsViewModel>()
            UiEventConsumer(snackbarHostState, viewModel.events)
            val state by viewModel.state.collectAsStateWithLifecycle()
            val profileVM = hiltViewModel<ProfileViewModel>()
            UiEventConsumer(snackbarHostState, profileVM.events)
            val profileState by profileVM.state.collectAsStateWithLifecycle()
            SettingsScreen(
                modifier = modifier,
                state = state,
                onAction = viewModel::onAction,
                profileState = profileState,
                profileOnAction = profileVM::onAction,
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items: List<BottomNavItem> = state

    val onItemClick = { navItem: BottomNavItem ->
        //navController.navigate(it.route)
        navController.navigate(navItem.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        viewModel.decreaseBadge(navItem.badgeType)
    }

    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationBar(
        modifier = modifier,
        containerColor = Color.DarkGray,
        //containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 5.dp
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Green,
                    selectedTextColor = Color.Green,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent // opcjonalnie,,,,
                ),
                icon = {
                    Column(horizontalAlignment = CenterHorizontally) {
                        BadgedBox(
                            badge = {
                                if (item.badgeCount > 0) {
                                    Badge(
                                        modifier = Modifier.offset(x = 12.dp, y = (-12).dp),
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text(item.badgeCount.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(id = item.nameResId)
                            )
                        }
                    }
                },
                label = {
                    //if (selected) {
                    Text(
                        text = stringResource(id = item.nameResId),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp
                    )
                    //}
                },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
fun AppNavigationRail(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items: List<BottomNavItem> = state

    val onItemClick = { navItem: BottomNavItem ->
        //navController.navigate(it.route)
        navController.navigate(navItem.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        viewModel.decreaseBadge(navItem.badgeType)
    }

    val backStackEntry = navController.currentBackStackEntryAsState()

    AppNavigationRail(
        modifier,
        items,
        backStackEntry.value?.destination?.route,
        onItemClick
    )
}


@Composable
fun AppNavigationRail(
    modifier: Modifier = Modifier,
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = Color.DarkGray,
        //containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0, 0, 0, 0),
        //tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(
                14.dp,
                alignment = Alignment.CenterVertically
            ),
            //horizontalAlignment = CenterHorizontally
        ) {

            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationRailItem(
                    selected = selected,
                    onClick = { onItemClick(item) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.Green,
                        selectedTextColor = Color.Green,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent // opcjonalnie,,,,
                    ),
                    icon = {
                        BadgedBox(
                            badge = {
                                if (item.badgeCount > 0) {
                                    Badge {
                                        Text(item.badgeCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.nameResId)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(item.nameResId),
                            fontSize = 12.sp
                        )
                    },
                    alwaysShowLabel = true
                )
            }
        }
        //Spacer(modifier = Modifier.weight(1f)) // ⬆ wypycha w dół

        //Spacer(modifier = Modifier.weight(1f)) // ⬇ wypycha w górę
    }
}


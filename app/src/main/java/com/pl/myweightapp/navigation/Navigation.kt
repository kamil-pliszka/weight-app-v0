package com.pl.myweightapp.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pl.myweightapp.xxx.history.HistoryScreen
import com.pl.myweightapp.xxx.home.HomeScreen
import com.pl.myweightapp.xxx.settings.SettingsScreen

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home")
    object HistoryScreen : Screen("history")
    object SettingsScreen : Screen("settings")
}

@Composable
fun Navigation(modifier: Modifier = Modifier, navController: NavHostController, snackbarHostState: SnackbarHostState) {
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(
                modifier = modifier,
                snackbarHostState = snackbarHostState
            )
        }
        composable(route = Screen.HistoryScreen.route) {
            HistoryScreen(
                modifier = modifier,
                snackbarHostState = snackbarHostState
            )
        }
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(
                modifier = modifier,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)


val BOTTOM_NAV = listOf(
    BottomNavItem(
        name = "Home",
        route = Screen.HomeScreen.route,
        icon = Icons.Default.Home,
    ),
    BottomNavItem(
        name = "History",
        route = Screen.HistoryScreen.route,
        //icon = Icons.Default.Notifications,
        icon = Icons.AutoMirrored.Filled.List,
    ),
    BottomNavItem(
        name = "Settings",
        route = Screen.SettingsScreen.route,
        icon = Icons.Default.Settings,
    )
)


@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: NavigationViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items: List<BottomNavItem> = state

    val onItemClick = { route: String ->
        //navController.navigate(it.route)
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        viewModel.decreaseBadge(route)
    }

    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationBar(
        modifier = modifier,
        containerColor = Color.DarkGray,
        tonalElevation = 5.dp
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
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
                                contentDescription = item.name
                            )
                        }
                        //if (selected) {
                            Text(
                                text = item.name,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        //}
                    }
                }
            )
        }
    }
}


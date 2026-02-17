package com.pl.myweightapp.navigation

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BottomNavItem(
    val nameResId: Int,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)


class NavigationViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(
        listOf(
            BottomNavItem(
                nameResId = R.string.navigation_home,
                route = Screen.HomeScreen.route,
                icon = Icons.Default.Home,
            ),
            BottomNavItem(
                nameResId = R.string.navigation_history,
                route = Screen.HistoryScreen.route,
                //icon = Icons.Default.Notifications,
                icon = Icons.AutoMirrored.Filled.List,
            ),
            BottomNavItem(
                nameResId = R.string.navigation_settings,
                route = Screen.SettingsScreen.route,
                icon = Icons.Default.Settings,
            )
        )
    )

    //val state = _state.asStateFlow()
//    val state = _state
//        .onStart { loadInitialBadges() }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000L),
//            BOTTOM_NAV
//        )
    val state = _state.asStateFlow()


    init {
        loadInitialBadges()
    }


    private fun loadInitialBadges() {
        viewModelScope.launch {
            //TODO - to jest wersja demo, ogarnąć
            launch(Dispatchers.IO) {
                val count = initialHomeBadgeCount()
                updateBadge(Screen.HomeScreen.route, count)
            }

            launch(Dispatchers.IO) {
                val count = initialHistoryBadgeCount()
                updateBadge(Screen.HistoryScreen.route, count)
            }

            launch(Dispatchers.IO) {
                val count = initialSettingsBadgeCount()
                updateBadge(Screen.SettingsScreen.route, count)
            }
        }
    }

    private suspend fun initialHomeBadgeCount(): Int {
        delay(3000)
        return 1
    }

    private suspend fun initialHistoryBadgeCount(): Int {
        delay(1800)
        return 0
    }

    private suspend fun initialSettingsBadgeCount(): Int {
        delay(1000)
        return 13
    }

    fun decreaseBadge(route: String) {
        _state.update {
            it.map { item ->
                if (item.route == route) {
                    item.copy(badgeCount = (item.badgeCount - 1).coerceAtLeast(0))
                } else {
                    item
                }
            }
        }
    }

    private fun updateBadge(route: String, count: Int) {
        _state.update {
            it.map { item ->
                if (item.route == route) {
                    item.copy(badgeCount = count.coerceAtLeast(0))
                } else {
                    item
                }
            }
        }
    }
}
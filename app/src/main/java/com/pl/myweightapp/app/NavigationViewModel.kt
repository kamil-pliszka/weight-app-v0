package com.pl.myweightapp.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.NavigationBadgeRepository
import com.pl.myweightapp.domain.NavigationBadges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BottomNavItem(
    val nameResId: Int,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val badgeType: BadgeType,
)

enum class BadgeType {
    HOME,
    HISTORY,
    SETTINGS
}

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val navBadgeRepo: NavigationBadgeRepository
) : ViewModel() {
//    val badges = navNadgeRepo
//        .observeBadges()
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(),
//            NavigationBadges(0, 0, 0)
//        ).onEach { bs ->
//            println("Got badges change: $bs")
//
//        }

    private val baseItems = listOf(
        BottomNavItem(
            nameResId = R.string.navigation_home,
            route = Screen.HomeScreen.route,
            icon = Icons.Default.Home,
            badgeType = BadgeType.HOME,
        ),
        BottomNavItem(
            nameResId = R.string.navigation_history,
            route = Screen.HistoryScreen.route,
            icon = Icons.AutoMirrored.Filled.List,
            badgeType = BadgeType.HISTORY,
        ),
        BottomNavItem(
            nameResId = R.string.navigation_settings,
            route = Screen.SettingsScreen.route,
            icon = Icons.Default.Settings,
            badgeType = BadgeType.SETTINGS,
        )
    )

    fun NavigationBadges.get(type: BadgeType): Int = when(type) {
        BadgeType.HOME -> home
        BadgeType.HISTORY -> history
        BadgeType.SETTINGS -> settings
    }

    val state: StateFlow<List<BottomNavItem>> = navBadgeRepo
            .observeBadges()
            .map { badges ->
                baseItems.map { item ->
                    item.copy(badgeCount = badges.get(item.badgeType))
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                baseItems
            )


    //val state = _state.asStateFlow()


    //val state = _state.asStateFlow()
//    val state = _state
//        .onStart { loadInitialBadges() }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000L),
//            BOTTOM_NAV
//        )


    //    init {
//        loadInitialBadges()
//    }
//    private fun loadInitialBadges() {
//        viewModelScope.launch {
//            launch(Dispatchers.IO) {
//                val count = initialHomeBadgeCount()
//                updateBadge(Screen.HomeScreen.route, count)
//            }
//
//            launch(Dispatchers.IO) {
//                val count = initialHistoryBadgeCount()
//                updateBadge(Screen.HistoryScreen.route, count)
//            }
//
//            launch(Dispatchers.IO) {
//                val count = initialSettingsBadgeCount()
//                updateBadge(Screen.SettingsScreen.route, count)
//            }
//        }
//    }
//
//    private suspend fun initialHomeBadgeCount(): Int {
//        delay(3000)
//        return 1
//    }
//
//    private suspend fun initialHistoryBadgeCount(): Int {
//        delay(1800)
//        return 0
//    }
//
//    private suspend fun initialSettingsBadgeCount(): Int {
//        delay(1000)
//        return 13
//    }
//
//    fun decreaseBadge(route: String) {
//        _state.update {
//            it.map { item ->
//                if (item.route == route) {
//                    item.copy(badgeCount = (item.badgeCount - 1).coerceAtLeast(0))
//                } else {
//                    item
//                }
//            }
//        }
//    }
//
//    private fun updateBadge(route: String, count: Int) {
//        _state.update {
//            it.map { item ->
//                if (item.route == route) {
//                    item.copy(badgeCount = count.coerceAtLeast(0))
//                } else {
//                    item
//                }
//            }
//        }
//    }
    fun decreaseBadge(badgeType: BadgeType) {
        viewModelScope.launch {
            when(badgeType) {
                BadgeType.HOME -> navBadgeRepo.decreaseHomeBadgeCount()
                BadgeType.HISTORY -> navBadgeRepo.decreaseHistoryBadgeCount()
                BadgeType.SETTINGS -> navBadgeRepo.decreaseSettingsBadgeCount()
            }
        }
    }
}
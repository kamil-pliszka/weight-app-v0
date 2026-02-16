package com.pl.myweightapp.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NavigationViewModel : ViewModel() {
    private val _state = MutableStateFlow<List<BottomNavItem>>(BOTTOM_NAV)
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
        return 1;
    }

    private suspend fun initialHistoryBadgeCount(): Int {
        delay(1800)
        return 0;
    }

    private suspend fun initialSettingsBadgeCount(): Int {
        delay(1000)
        return 13;
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
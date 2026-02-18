package com.pl.myweightapp.data.repository

import com.pl.myweightapp.navigation.NavigationBadges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NavigationBadgeRepository {
    private val _state = MutableStateFlow(
        NavigationBadges(
            home = 2,
            history = 1,
            settings = 8
        )
    )

    fun observeBadges(): Flow<NavigationBadges> = _state.asStateFlow()

    suspend fun decreaseHomeBadgeCount() = withContext(Dispatchers.IO) {
        launch {
            delay(3000)
            _state.update {
                it.copy(home = (it.home - 1).coerceAtLeast(0))
            }
        }
    }

    suspend fun decreaseHistoryBadgeCount() = withContext(Dispatchers.IO) {
        launch {
            delay(1000)
            _state.update {
                it.copy(history = (it.history - 1).coerceAtLeast(0))
            }
        }
    }

    suspend fun decreaseSettingsBadgeCount() = withContext(Dispatchers.IO) {
        launch {
            delay(2000)
            _state.update {
                it.copy(settings = (it.settings - 1).coerceAtLeast(0))
            }
        }
    }


}
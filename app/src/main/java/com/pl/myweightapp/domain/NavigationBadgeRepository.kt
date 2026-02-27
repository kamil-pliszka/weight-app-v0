package com.pl.myweightapp.domain

import kotlinx.coroutines.flow.Flow

interface NavigationBadgeRepository {
    fun observeBadges(): Flow<NavigationBadges>
    suspend fun decreaseHomeBadgeCount()
    suspend fun decreaseHistoryBadgeCount()
    suspend fun decreaseSettingsBadgeCount()
}
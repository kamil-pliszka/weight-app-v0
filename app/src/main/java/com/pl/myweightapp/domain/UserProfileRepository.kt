package com.pl.myweightapp.domain

import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun save(profile: UserProfile)
    suspend fun deleteAll()
    suspend fun hasAny(): Boolean
}
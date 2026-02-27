package com.pl.myweightapp.data.repository

import com.pl.myweightapp.data.local.UserProfileDao
import com.pl.myweightapp.data.mappers.toUserProfile
import com.pl.myweightapp.data.mappers.toUserProfileEntity
import com.pl.myweightapp.domain.UserProfile
import com.pl.myweightapp.domain.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserProfileRepositoryImpl(
    private val dao: UserProfileDao
) : UserProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> {
        return dao.observeProfile().map { it?.toUserProfile() }
    }

    override suspend fun save(profile: UserProfile) = withContext(Dispatchers.IO) {
        dao.upsert(profile.toUserProfileEntity().copy(id = 0)) //może być tylko jeden rekord z id = 0
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }

    override suspend fun hasAny(): Boolean = withContext(Dispatchers.IO) {
        dao.hasAny()
    }

    /*
    suspend fun updatePeriod(period: DisplayPeriod) = withContext(Dispatchers.IO) {
        val current = dao.getProfile()

        val updated = current?.copy(displayPeriod = period)
            ?: UserProfileEntity(id = 0, displayPeriod = period)
        dao.upsert(updated)
    }

    suspend fun updateMovingAverages(ma1: Int?, ma2: Int?) = withContext(Dispatchers.IO) {
        val current = dao.getProfile()

        val updated = current?.copy(movingAverage1 = ma1, movingAverage2 = ma2)
            ?: UserProfileEntity(id = 0, movingAverage1 = ma1, movingAverage2 = ma2)
        dao.upsert(updated)
    }
    */

    /*
    suspend fun updateLang(lang: String?) = withContext(Dispatchers.IO) {
        val current = dao.getProfile()

        val updated = current?.copy(lang = lang)
            ?: UserProfileEntity(id = 0, lang = lang)
        dao.upsert(updated)
    }

    suspend fun getLang(): String? = withContext(Dispatchers.IO) {
        dao.getLang()
    }
    */
}
package com.pl.myweightapp.data.repository

import com.pl.myweightapp.data.local.UserProfileDao
import com.pl.myweightapp.data.local.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserProfileRepository(
    private val dao: UserProfileDao
) {
    val profile = dao.observeProfile()

    suspend fun save(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        dao.upsert(profile.copy(id = 0)) //może być tylko jeden rekord z id = 0
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO){
        dao.deleteAll()
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
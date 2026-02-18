package com.pl.myweightapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM ${UserProfileEntity.TABLE} WHERE id = 0 LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM ${UserProfileEntity.TABLE} WHERE id = 0 LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProfileEntity)

    @Query("DELETE FROM ${UserProfileEntity.TABLE}")
    suspend fun deleteAll()

//    @Query("SELECT lang FROM ${UserProfileEntity.TABLE} WHERE id = 0 LIMIT 1")
//    suspend fun getLang(): String?

}
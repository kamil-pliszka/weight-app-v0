package com.pl.myweightapp.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal


// (3) Definicja Data Access Object
// @Dao - adnotacja oznaczająca klasę jako DAO, powoduje te wygenerowaniu kodu implementującego
// DAO na podstawie interfejsu 'CurrentWeatherDao' oraz metod w nim zawartych
// @Insert - adnotacja oznaczająca metodę SQL INSERT powodująca wstawienie nowego rekordu do tabeli
// @Delete - adnotacja oznaczająca metodę SQL DELETE powodująca usunięcie rekordu z tabeli
@Dao
interface WeightMeasureDao {
    // INSERT, DELETE
    @Insert//(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(measureEntity: WeightMeasureEntity)

    @Update
    suspend fun update(entity: WeightMeasureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(entities: List<WeightMeasureEntity>)

    @Update
    suspend fun updateAll(entities: List<WeightMeasureEntity>)

    @Delete
    suspend fun delete(measureEntity: WeightMeasureEntity)

    @Transaction
    suspend fun importAll(
        toInsert: List<WeightMeasureEntity>,
        toUpdate: List<WeightMeasureEntity>
    ) {
        saveAll(toInsert)
        updateAll(toUpdate)
    }

    @Query("DELETE FROM ${WeightMeasureEntity.TABLE} WHERE id = :id")
    suspend fun deleteById(id: Long) : Int

    @Query("DELETE FROM ${WeightMeasureEntity.TABLE}")
    suspend fun deleteAll()

    @Query("""
        SELECT * FROM ${WeightMeasureEntity.TABLE}
        ORDER BY date DESC, id DESC
        LIMIT 1
    """)
    suspend fun findLastMeasureEntity(): WeightMeasureEntity?

    @Query("""
        SELECT weight FROM ${WeightMeasureEntity.TABLE}
        ORDER BY date DESC, id DESC
        LIMIT 1
    """)
    suspend fun findLastWeightMeasure(): BigDecimal?
    @Query("""
        SELECT weight, unit FROM ${WeightMeasureEntity.TABLE}
        ORDER BY date DESC, id DESC
        LIMIT 1
    """)
    suspend fun findLastWeightMeasureAndUnit(): LastWeightMeasure?
    @Query("""
        SELECT * FROM ${WeightMeasureEntity.TABLE}
        ORDER BY date DESC, id DESC
    """)
    suspend fun findWeightMeasureHistory(): List<WeightMeasureEntity>

    @Query("SELECT * FROM ${WeightMeasureEntity.TABLE} ORDER BY date DESC, id DESC")
    fun observeWeightMeasureHistory(): Flow<List<WeightMeasureEntity>>

    @Query("""
    SELECT *, 
        weight - LAG(weight) OVER (ORDER BY date ASC, id ASC) AS change
    FROM ${WeightMeasureEntity.TABLE}
    ORDER BY date DESC, id DESC
    """)
    fun pagingSourceWithChange(): PagingSource<Int, WeightMeasureWithChange>

    @Query("""
        SELECT * 
        FROM ${WeightMeasureEntity.TABLE}
        WHERE id = :id
        LIMIT 1
    """)
    fun observeById(id: Long): Flow<WeightMeasureEntity?>
    @Query("""
        SELECT EXISTS(SELECT 1 FROM ${WeightMeasureEntity.TABLE})
    """)
    suspend fun hasAny(): Boolean

}
package com.pl.myweightapp.repositories

import androidx.room.withTransaction
import com.pl.myweightapp.persistence.MyDatabase
import com.pl.myweightapp.persistence.WeightMeasureDao
import com.pl.myweightapp.persistence.WeightMeasureEntity
import com.pl.myweightapp.persistence.WeightUnit
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.Instant

class WeightMeasureRepository(val weightMeasureDao: WeightMeasureDao) {
    //private val database = MyAppContainer.provideMyAppDatabase()

    suspend fun insertMeasure(
        date: Instant,
        weight: BigDecimal,
        unit: WeightUnit = WeightUnit.KG
    ) {
        val entity = WeightMeasureEntity(
            date = date,
            weight = weight,
            unit = unit
        )
        weightMeasureDao.save(entity)
        //if (true) throw NullPointerException()
    }

    suspend fun update(
        id: Long,
        date: Instant,
        weight: BigDecimal,
        unit: WeightUnit
    ) {
        weightMeasureDao.update(
            WeightMeasureEntity(
                id = id,
                date = date,
                weight = weight,
                unit = unit
            )
        )
    }

    suspend fun update(entity: WeightMeasureEntity) {
        weightMeasureDao.update(entity)
    }

    suspend fun import(
        db: MyDatabase,
        toInsert: List<WeightMeasureEntity>,
        toUpdate: List<WeightMeasureEntity>
    ) {
        db.withTransaction {
            toInsert.forEach {entity ->
                weightMeasureDao.save(entity)
            }
            toUpdate.forEach {entity ->
                weightMeasureDao.update(entity)
            }
        }
    }


    suspend fun findLastWeightMeasure(): BigDecimal? {
        return weightMeasureDao.findLastWeightMeasure()
    }

    suspend fun findWeightMeasureHistory(): List<WeightMeasureEntity> {
        return weightMeasureDao.findWeightMeasureHistory()
    }

    fun observeWeightMeasureHistory(): Flow<List<WeightMeasureEntity>> =
        weightMeasureDao.observeWeightMeasureHistory()

    fun observeById(id: Long): Flow<WeightMeasureEntity?> = weightMeasureDao.observeById(id)

    suspend fun delete(id: Long) {
        val rows = weightMeasureDao.deleteById(id)
        if (rows == 0) {
            throw IllegalStateException("Delete failed: id=$id not found")
        }
    }

    suspend fun deleteAll() {
        weightMeasureDao.deleteAll()
    }

}

fun sortWeightMeasureHistory(history: List<WeightMeasureEntity>): List<WeightMeasureEntity> {
    return history.sortedWith(
        compareByDescending<WeightMeasureEntity> { it.date }
            .thenByDescending { it.id }
    )
}


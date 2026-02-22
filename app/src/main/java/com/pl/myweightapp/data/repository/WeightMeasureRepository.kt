package com.pl.myweightapp.data.repository

import com.pl.myweightapp.core.domain.WeightUnit
import com.pl.myweightapp.data.local.LastWeightMeasure
import com.pl.myweightapp.data.local.WeightMeasureDao
import com.pl.myweightapp.data.local.WeightMeasureEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant

class WeightMeasureRepository(val weightMeasureDao: WeightMeasureDao) {
    //private val database = MyAppContainer.provideMyAppDatabase()

    suspend fun insertMeasure(
        date: Instant,
        weight: BigDecimal,
        unit: WeightUnit = WeightUnit.KG
    ) = withContext(Dispatchers.IO) {
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
    ) = withContext(Dispatchers.IO) {
        weightMeasureDao.update(
            WeightMeasureEntity(
                id = id,
                date = date,
                weight = weight,
                unit = unit
            )
        )
    }

    suspend fun update(entity: WeightMeasureEntity) = withContext(Dispatchers.IO) {
        weightMeasureDao.update(entity)
    }

    suspend fun import(
        toInsert: List<WeightMeasureEntity>,
        toUpdate: List<WeightMeasureEntity>
    ) = withContext(Dispatchers.IO) {
        weightMeasureDao.importAll(toInsert, toUpdate)
    }


    suspend fun findLastWeightMeasure(): BigDecimal? = withContext(Dispatchers.IO) {
        weightMeasureDao.findLastWeightMeasure()
    }

    suspend fun findLastWeightMeasureAndUnit(): LastWeightMeasure? = withContext(Dispatchers.IO) {
        weightMeasureDao.findLastWeightMeasureAndUnit()
    }

    suspend fun findWeightMeasureHistory(): List<WeightMeasureEntity> = withContext(Dispatchers.IO) {
        weightMeasureDao.findWeightMeasureHistory()
    }

    fun observeWeightMeasureHistory(): Flow<List<WeightMeasureEntity>> =
        weightMeasureDao.observeWeightMeasureHistory()

    fun observeById(id: Long): Flow<WeightMeasureEntity?> = weightMeasureDao.observeById(id)

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        val rows = weightMeasureDao.deleteById(id)
        if (rows == 0) {
            throw IllegalStateException("Delete failed: id=$id not found")
        }
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        weightMeasureDao.deleteAll()
    }

}

fun sortWeightMeasureHistory(history: List<WeightMeasureEntity>): List<WeightMeasureEntity> {
    return history.sortedWith(
        compareByDescending<WeightMeasureEntity> { it.date }
            .thenByDescending { it.id }
    )
}


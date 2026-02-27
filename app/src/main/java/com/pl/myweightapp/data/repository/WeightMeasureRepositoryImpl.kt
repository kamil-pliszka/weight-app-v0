package com.pl.myweightapp.data.repository

import com.pl.myweightapp.data.local.WeightMeasureDao
import com.pl.myweightapp.data.local.WeightMeasureEntity
import com.pl.myweightapp.data.mappers.toWeightMeasure
import com.pl.myweightapp.data.mappers.toWeightMeasureEntity
import com.pl.myweightapp.domain.WeightMeasure
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.domain.WeightUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant

class WeightMeasureRepositoryImpl(
    val weightMeasureDao: WeightMeasureDao
) : WeightMeasureRepository {
    //private val database = MyAppContainer.provideMyAppDatabase()

    override suspend fun insertMeasure(
        date: Instant,
        weight: BigDecimal,
        unit: WeightUnit
    ) = withContext(Dispatchers.IO) {
        val entity = WeightMeasureEntity(
            date = date,
            weight = weight,
            unit = unit
        )
        weightMeasureDao.save(entity)
        //if (true) throw NullPointerException()
    }

    override suspend fun update(
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

    override suspend fun update(toUpdate: WeightMeasure) = withContext(Dispatchers.IO) {
        weightMeasureDao.update(toUpdate.toWeightMeasureEntity())
    }

    override suspend fun import(
        toInsert: List<WeightMeasure>,
        toUpdate: List<WeightMeasure>
    ) = withContext(Dispatchers.IO) {
        weightMeasureDao.importAll(
            toInsert.map { it.toWeightMeasureEntity() },
            toUpdate.map { it.toWeightMeasureEntity() },
        )
    }


    override suspend fun findLastWeightMeasure(): BigDecimal? = withContext(Dispatchers.IO) {
        weightMeasureDao.findLastWeightMeasure()
    }

    override suspend fun findLastWeightMeasureAndUnit(): Pair<BigDecimal, WeightUnit>? = withContext(Dispatchers.IO) {
        weightMeasureDao.findLastWeightMeasureAndUnit()?.let {
            Pair(it.weight, it.unit)
        }
    }

    override suspend fun findWeightMeasureHistory(): List<WeightMeasure> = withContext(Dispatchers.IO) {
        weightMeasureDao.findWeightMeasureHistory().map { it.toWeightMeasure() }
    }

    override fun observeWeightMeasureHistory(): Flow<List<WeightMeasure>> =
        weightMeasureDao.observeWeightMeasureHistory().map {
            list -> list.map { it.toWeightMeasure() }
        }

    override fun observeById(id: Long): Flow<WeightMeasure?> =
        weightMeasureDao.observeById(id).map {
            it?.toWeightMeasure()
        }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        val rows = weightMeasureDao.deleteById(id)
        if (rows == 0) {
            throw IllegalStateException("Delete failed: id=$id not found")
        }
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        weightMeasureDao.deleteAll()
    }

    override suspend fun hasAny(): Boolean = withContext(Dispatchers.IO) {
        weightMeasureDao.hasAny()
    }

}

fun sortWeightMeasureHistory(history: List<WeightMeasureEntity>): List<WeightMeasureEntity> {
    return history.sortedWith(
        compareByDescending<WeightMeasureEntity> { it.date }
            .thenByDescending { it.id }
    )
}


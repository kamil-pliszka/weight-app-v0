package com.pl.myweightapp.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.Instant

interface WeightMeasureRepository {
    suspend fun insertMeasure(
        date: Instant,
        weight: BigDecimal,
        unit: WeightUnit = WeightUnit.KG
    )

    suspend fun update(
        id: Long,
        date: Instant,
        weight: BigDecimal,
        unit: WeightUnit
    )

    suspend fun update(toUpdate: WeightMeasure)

    suspend fun import(
        toInsert: List<WeightMeasure>,
        toUpdate: List<WeightMeasure>
    )

    suspend fun findLastWeightMeasure(): BigDecimal?

    suspend fun findLastWeightMeasureAndUnit(): Pair<BigDecimal, WeightUnit>?

    suspend fun findWeightMeasureHistory(): List<WeightMeasure>

    fun observeWeightMeasureHistory(): Flow<List<WeightMeasure>>

    fun getPagedHistory(): Flow<PagingData<Pair<WeightMeasure, BigDecimal?>>>

    fun observeById(id: Long): Flow<WeightMeasure?>

    suspend fun delete(id: Long)

    suspend fun deleteAll()

    suspend fun hasAny(): Boolean
}
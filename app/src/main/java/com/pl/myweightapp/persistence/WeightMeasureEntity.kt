package com.pl.myweightapp.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.Instant


// (2) - Zdefiniowanie entity - tj. struktury danych którą będziemy przechowywać w tabeli
@Entity(tableName = WeightMeasureEntity.TABLE)
data class WeightMeasureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Instant,
    val weight: BigDecimal,
    val unit: WeightUnit,
    ) {
    companion object {
        const val TABLE = "weight_measure"
    }
}
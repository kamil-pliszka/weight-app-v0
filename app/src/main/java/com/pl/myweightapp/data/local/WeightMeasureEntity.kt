package com.pl.myweightapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pl.myweightapp.core.domain.WeightUnit
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

data class LastWeightMeasure(
    val weight: BigDecimal,
    val unit: WeightUnit
)
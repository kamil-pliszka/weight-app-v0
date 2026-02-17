package com.pl.myweightapp.persistence

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.Instant

class DbTypeConverters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    // Enum
    @TypeConverter
    fun fromWeightUnit(unit: WeightUnit?): String? =
        unit?.name

    @TypeConverter
    fun toWeightUnit(value: String?): WeightUnit? =
        value?.let { WeightUnit.valueOf(it) }

    // BigDecimal
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? =
        value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? =
        value?.toBigDecimal()

    // Enum
    @TypeConverter
    fun fromHeightUnit(unit: HeightUnit?): String? =
        unit?.name

    @TypeConverter
    fun toHeightUnit(value: String?): HeightUnit? =
        value?.let { HeightUnit.valueOf(it) }

    @TypeConverter
    fun fromGender(unit: Gender?): String? =
        unit?.name

    @TypeConverter
    fun toGender(value: String?): Gender? =
        value?.let { Gender.valueOf(it) }

//    @TypeConverter
//    fun fromDisplayPeriod(period: DisplayPeriod?): String? =
//        period?.name
//
//    @TypeConverter
//    fun toDisplayPeriod(value: String?): DisplayPeriod? =
//        value?.let { DisplayPeriod.valueOf(it) }

}
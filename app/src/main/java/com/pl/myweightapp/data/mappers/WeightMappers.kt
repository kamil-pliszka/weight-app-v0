package com.pl.myweightapp.data.mappers

import com.pl.myweightapp.data.local.WeightMeasureEntity
import com.pl.myweightapp.domain.WeightMeasure

fun WeightMeasure.toWeightMeasureEntity(): WeightMeasureEntity {
    return WeightMeasureEntity(
        id = id,
        date = date,
        weight = weight,
        unit = unit,
    )
}

fun WeightMeasureEntity.toWeightMeasure(): WeightMeasure {
    return WeightMeasure(
        id = id,
        date = date,
        weight = weight,
        unit = unit,
    )
}
package com.pl.myweightapp.data.csv

import com.pl.myweightapp.domain.WeightUnit

enum class WeightUnitCsv { KG, LB }

fun WeightUnitCsv.toDomainWeightUnit(): WeightUnit {
    return when (this) {
        WeightUnitCsv.KG -> WeightUnit.KG
        WeightUnitCsv.LB -> WeightUnit.LB
    }
}
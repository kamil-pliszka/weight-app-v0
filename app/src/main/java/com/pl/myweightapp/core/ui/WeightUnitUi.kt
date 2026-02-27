package com.pl.myweightapp.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.WeightUnit

enum class WeightUnitUi { KG, LB }


@Composable
fun WeightUnit.label(): String = stringResource(toResourceId())
//fun WeightUnit.label(): String = when (this) {
//    WeightUnit.KG -> stringResource(R.string.weight_unit_kg)
//    WeightUnit.LB -> stringResource(R.string.weight_unit_lb)
//}

@Composable
fun WeightUnitUi.label(): String = stringResource(toResourceId())

fun WeightUnit.toWeightUnitUi() : WeightUnitUi {
    return when(this) {
        WeightUnit.KG -> WeightUnitUi.KG
        WeightUnit.LB -> WeightUnitUi.LB
    }
}

fun WeightUnitUi.toWeightUnit(): WeightUnit {
    return when (this) {
        WeightUnitUi.KG -> WeightUnit.KG
        WeightUnitUi.LB -> WeightUnit.LB
    }
}

fun WeightUnitUi.toResourceId(): Int {
    return when (this) {
        WeightUnitUi.KG -> R.string.weight_unit_kg
        WeightUnitUi.LB -> R.string.weight_unit_lb
    }
}
fun WeightUnit.toResourceId(): Int {
    return when (this) {
        WeightUnit.KG -> R.string.weight_unit_kg
        WeightUnit.LB -> R.string.weight_unit_lb
    }
}

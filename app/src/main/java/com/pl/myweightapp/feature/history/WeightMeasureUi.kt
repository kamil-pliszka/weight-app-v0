package com.pl.myweightapp.feature.history

import android.icu.text.NumberFormat
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.pl.myweightapp.R
import com.pl.myweightapp.core.util.toDateString
import com.pl.myweightapp.data.local.WeightMeasureEntity
import com.pl.myweightapp.core.domain.WeightUnit
import java.math.BigDecimal
import java.time.Instant
import java.util.Locale

enum class WeightUnitUi { KG, LB }

@Immutable
data class WieghtMeasureUi(
    val id: Long,
    val date: DisplayableValue<Instant>,
    val weight: DisplayableValue<BigDecimal>,
    //val unit: DisplayableValue<WeightUnit>,
    val unit: WeightUnitUi,
    val change: DisplayableValue<BigDecimal>?,
)

data class DisplayableValue<T>(
    val value: T,
    val formatted: String
)

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

fun Instant.toDisplayableInstant() = DisplayableValue(this, this.toDateString())


fun Double.toDisplayableNumber(): DisplayableValue<Double> {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return DisplayableValue(
        value = this,
        formatted = formatter.format(this)
    )
}

fun BigDecimal.toDisplayableNumber() =
    DisplayableValue(
        this,
        this.toPlainString()
    )

fun BigDecimal.toDisplayableNumberWithSign() =
    DisplayableValue(
        this,
        toPlainString().let {
            if (signum() > 0) "+$it" else it
        }
    )


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

fun WeightMeasureEntity.toWeightMeasureUi(change: BigDecimal? = null): WieghtMeasureUi {
    return WieghtMeasureUi(
        id = id,
        date = date.toDisplayableInstant(),
        weight = weight.toDisplayableNumber(),
        unit = unit.toWeightUnitUi(),
        change = change?.toDisplayableNumberWithSign(),
    )
}

fun BigDecimal?.weightChangeColor(): Color =
    when (this?.signum()) {
        1 -> Color.Red
        -1 -> Color(0xFF2E7D32) //Color.Green
        else -> Color.Black
    }
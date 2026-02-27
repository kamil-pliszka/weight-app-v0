package com.pl.myweightapp.domain

import com.pl.myweightapp.core.util.kgToLbs
import com.pl.myweightapp.core.util.lbsToKg
import com.pl.myweightapp.core.util.toFloat1
import java.math.BigDecimal

enum class WeightUnit {
    KG,
    LB
}

fun BigDecimal.convertValueTo(srcUnit: WeightUnit, dstUnit: WeightUnit): Float {
    val weight = this.toFloat1()
    return when {
        srcUnit == dstUnit -> weight
        dstUnit == WeightUnit.KG && srcUnit == WeightUnit.LB -> weight.lbsToKg()
        dstUnit == WeightUnit.LB && srcUnit == WeightUnit.KG -> weight.kgToLbs()
        else -> error("Unknown conversion from: $srcUnit to: $dstUnit")
    }
}
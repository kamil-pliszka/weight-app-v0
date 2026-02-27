package com.pl.myweightapp.domain

import java.math.BigDecimal

data class UserProfile(
    val age: Int? = null,
    val height: Int? = null,
    val heightUnit: HeightUnit? = null,
    val targetWeight: BigDecimal? = null,
    val weightUnit: WeightUnit? = null,
    val gender: Gender? = null,
    val photoPath: String? = null, // ścieżka do pliku
    val name: String? = null,
)

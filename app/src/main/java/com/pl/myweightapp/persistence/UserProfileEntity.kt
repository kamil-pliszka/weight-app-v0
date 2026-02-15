package com.pl.myweightapp.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Śmietnik zwany profilem użytkownika
 */
@Entity(tableName = UserProfileEntity.TABLE)
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 0, // ZAWSZE 1 rekord
    val age: Int? = null,
    val height: Int? = null,
    val heightUnit: HeightUnit? = null,
    val targetWeight: BigDecimal? = null,
    val weightUnit: WeightUnit? = null,
    val gender: Gender? = null,
    val photoPath: String? = null, // ścieżka do pliku
    val name: String? = null,
    val displayPeriod: DisplayPeriod? = null,
    val movingAverage1: Int? = null,
    val movingAverage2: Int? = null,
    val lang: String? = null,
) {
    companion object {
        const val TABLE = "user_profile"
    }
}

enum class Gender {
    MALE,
    FEMALE,
    UNSPECIFIED
}

enum class HeightUnit {
    CM,
    IN
}

enum class DisplayPeriod {
    P2W, P1M, P2M, P3M, P6M, P1Y, P2Y, P3Y, ALL
}
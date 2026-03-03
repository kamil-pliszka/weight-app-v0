package com.pl.myweightapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// (4) Konstrukcja bazy danych przechowującej konkretne entity, w tym przypadku 'CurrentWeatherEntity'
// version - wersja bazy danych, wynikająca z wersji tabel w tje bazie (jeśli tabele (entities) ulegną zmianie, to trzeba
// zwiększyć wersję bazy danych)
// @Database - adnotacja oznaczająca klasę jako bazę danych, powoduje wygenerowanie kodu implementującego bazę danych
// na podstawie klasy abstrakcyjnej WeatherDatabase oraz metod w niej zawartych
@Database(entities = [WeightMeasureEntity::class, UserProfileEntity::class], version = 8, exportSchema = true)
@TypeConverters(DbTypeConverters::class)
abstract class WeightDatabase: RoomDatabase() {
    abstract fun weightMeasureDao(): WeightMeasureDao

    abstract fun userProfileDao(): UserProfileDao
}


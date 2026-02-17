package com.pl.myweightapp

import android.content.Context
import androidx.room.Room
import com.pl.myweightapp.persistence.MyDatabase
import com.pl.myweightapp.repositories.AppSettingsRepository
import com.pl.myweightapp.repositories.UserProfileRepository
import com.pl.myweightapp.repositories.WeightMeasureRepository
import kotlinx.coroutines.runBlocking

object AppModule {

    private lateinit var appContext: Context
    private lateinit var database: MyDatabase

    private lateinit var weightMeasureRepository: WeightMeasureRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var appSettingsManager: AppSettingsManager

    // Inicjalizacja kontenera Contextem - aby mieć dostęp do plików (w tym pliku bazy danych Room)
    fun initialize(context: Context) {
        appContext = context
        //context.deleteDatabase("my-app-database")//wywalić
        database = Room.databaseBuilder(appContext, MyDatabase::class.java, "my-app-database")
            .fallbackToDestructiveMigration(false)
            .build()

        weightMeasureRepository = WeightMeasureRepository(database.weightMeasureDao())
        userProfileRepository = UserProfileRepository(database.userProfileDao())

        val repo = AppSettingsRepository(context)
        appSettingsManager = AppSettingsManager(repo)
        runBlocking {
            appSettingsManager.bootstrapLang()
        }
    }

    // (6) Dostęp BD z obszaru całej aplikacji - zwraca instancję bazy danych
    fun provideMyDatabase(): MyDatabase {
        return database
    }

    fun provideWeightMeasureRepository() = weightMeasureRepository
    fun provideUserProfileRepository() = userProfileRepository
    fun provideAppSettingsManager() = appSettingsManager
}
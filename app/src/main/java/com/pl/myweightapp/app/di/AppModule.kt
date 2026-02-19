package com.pl.myweightapp.app.di

import android.content.Context
import androidx.room.Room
import com.pl.myweightapp.data.local.MyDatabase
import com.pl.myweightapp.data.preferences.AppSettingsDataSource
import com.pl.myweightapp.data.preferences.AppSettingsManager
import com.pl.myweightapp.data.repository.AppSettingsRepositoryImpl
import com.pl.myweightapp.data.repository.NavigationBadgeRepository
import com.pl.myweightapp.data.repository.UserProfileRepository
import com.pl.myweightapp.data.repository.WeightMeasureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

object AppModule {

    private lateinit var appContext: Context
    private lateinit var database: MyDatabase

    private lateinit var weightMeasureRepository: WeightMeasureRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var appSettingsManager: AppSettingsManager

    private lateinit var navBadgeRepository: NavigationBadgeRepository

    // Inicjalizacja kontenera Contextem - aby mieć dostęp do plików (w tym pliku bazy danych Room)
    fun initialize(context: Context) {
        appContext = context
        //context.deleteDatabase("my-app-database")//wywalić
        database = Room.databaseBuilder(appContext, MyDatabase::class.java, "my-app-database")
            .fallbackToDestructiveMigration(false)
            .build()

        weightMeasureRepository = WeightMeasureRepository(database.weightMeasureDao())
        userProfileRepository = UserProfileRepository(database.userProfileDao())
        navBadgeRepository = NavigationBadgeRepository()

        val dataSource = AppSettingsDataSource(context)
        val repo = AppSettingsRepositoryImpl(dataSource)
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        appSettingsManager = AppSettingsManager(repo, applicationScope)
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

    fun provideNavBadgeRepository() = navBadgeRepository
}
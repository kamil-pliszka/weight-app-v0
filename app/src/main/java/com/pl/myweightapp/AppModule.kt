package com.pl.myweightapp

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.room.Room
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.persistence.MyDatabase
import com.pl.myweightapp.repositories.UserProfileRepository
import com.pl.myweightapp.repositories.WeightMeasureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object AppModule {

    private lateinit var appContext: Context
    private lateinit var database: MyDatabase

    private lateinit var weightMeasureRepository: WeightMeasureRepository
    private lateinit var userProfileRepository: UserProfileRepository

    // Inicjalizacja kontenera Contextem - aby mieć dostęp do plików (w tym pliku bazy danych Room)
    fun initialize(context: Context) {
        appContext = context
        //context.deleteDatabase("my-app-database")//wywalić
        database = Room.databaseBuilder(appContext, MyDatabase::class.java, "my-app-database")
            .fallbackToDestructiveMigration(false)
            .build()

        weightMeasureRepository = WeightMeasureRepository(database.weightMeasureDao())
        userProfileRepository = UserProfileRepository(database.userProfileDao())
        runBlocking {
            initializeLangInternal()
        }
    }

    // (6) Dostęp BD z obszaru całej aplikacji - zwraca instancję bazy danych
    fun provideMyDatabase(): MyDatabase {
        return database
    }

    fun provideWeightMeasureRepository() = weightMeasureRepository
    fun provideUserProfileRepository() = userProfileRepository

    private suspend fun initializeLangInternal() {
        val lang = userProfileRepository.getLang() ?: Constants.DEFAULT_LANG
        println("initializeLang : $lang")
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(lang)
        )
    }
}
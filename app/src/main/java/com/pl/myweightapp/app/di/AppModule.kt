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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Context ---
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // --- Room Database ---
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MyDatabase {
        return Room.databaseBuilder(
            context,
            MyDatabase::class.java,
            "my-app-database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    // --- Repositories ---
    @Provides
    @Singleton
    fun provideWeightMeasureRepository(
        database: MyDatabase
    ): WeightMeasureRepository =
        WeightMeasureRepository(database.weightMeasureDao())

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        database: MyDatabase
    ): UserProfileRepository =
        UserProfileRepository(database.userProfileDao())

    @Provides
    @Singleton
    fun provideNavigationBadgeRepository(): NavigationBadgeRepository =
        NavigationBadgeRepository()

    // --- Settings ---

    @Provides
    @Singleton
    fun provideAppSettingsManager(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope
    ): AppSettingsManager {
        val dataSource = AppSettingsDataSource(context)
        val repo = AppSettingsRepositoryImpl(dataSource)
        return AppSettingsManager(repo, applicationScope)
    }

}
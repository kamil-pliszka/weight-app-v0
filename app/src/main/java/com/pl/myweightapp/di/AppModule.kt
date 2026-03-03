package com.pl.myweightapp.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.pl.myweightapp.core.Constants
import com.pl.myweightapp.data.StorageSupportImpl
import com.pl.myweightapp.data.chart.ChartImageManagerImpl
import com.pl.myweightapp.data.csv.CsvServiceImpl
import com.pl.myweightapp.data.local.BackupManager
import com.pl.myweightapp.data.local.WeightDatabase
import com.pl.myweightapp.data.preferences.AppSettingsDataSource
import com.pl.myweightapp.data.preferences.AppSettingsManager
import com.pl.myweightapp.data.repository.AppSettingsRepositoryImpl
import com.pl.myweightapp.data.repository.NavigationBadgeRepositoryImpl
import com.pl.myweightapp.data.repository.UserProfileRepositoryImpl
import com.pl.myweightapp.data.repository.WeightMeasureRepositoryImpl
import com.pl.myweightapp.domain.AppSettingsService
import com.pl.myweightapp.domain.BackupService
import com.pl.myweightapp.domain.NavigationBadgeRepository
import com.pl.myweightapp.domain.StorageSupport
import com.pl.myweightapp.domain.UserProfileRepository
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.domain.chart.ChartImageManager
import com.pl.myweightapp.domain.csv.CsvService
import com.pl.myweightapp.domain.usecase.ComputeHomeStateUseCase
import com.pl.myweightapp.domain.usecase.GenerateWeightChartDataUseCase
import com.pl.myweightapp.feature.home.chart.ChartRenderer
import com.pl.myweightapp.feature.home.chart.MpChartBitmapRenderer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.Executors
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
    ): WeightDatabase {
        return Room.databaseBuilder(
            context,
            WeightDatabase::class.java,
            "my-app-database"
        )
            .fallbackToDestructiveMigration(false)
            .apply {
                if (Constants.LOG_SQL) {
                    setQueryCallback(
                        { sqlQuery, bindArgs ->
                            Log.d("ROOM_SQL", "Query: $sqlQuery, args: $bindArgs")
                        },
                        Executors.newSingleThreadExecutor()
                    )
                }
            }
            .build()
    }

    // --- Repositories ---
    @Provides
    @Singleton
    fun provideWeightMeasureRepository(
        database: WeightDatabase
    ): WeightMeasureRepository =
        WeightMeasureRepositoryImpl(database.weightMeasureDao())

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        database: WeightDatabase
    ): UserProfileRepository =
        UserProfileRepositoryImpl(database.userProfileDao())

    @Provides
    @Singleton
    fun provideNavigationBadgeRepository(): NavigationBadgeRepository =
        NavigationBadgeRepositoryImpl()

    // --- Settings ---

    @Provides
    @Singleton
    fun provideAppSettingsService(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope
    ): AppSettingsService {
        val dataSource = AppSettingsDataSource(context)
        val repo = AppSettingsRepositoryImpl(dataSource)
        return AppSettingsManager(repo, applicationScope)
    }

    @Provides
    @Singleton
    fun provideGenerateWeightChartDataUseCase(): GenerateWeightChartDataUseCase =
        GenerateWeightChartDataUseCase()

    @Provides
    @Singleton
    fun provideComputeHomeStateUseCase(
        generateWeightChartDataUseCase: GenerateWeightChartDataUseCase
    ): ComputeHomeStateUseCase = ComputeHomeStateUseCase(generateWeightChartDataUseCase)

    @Provides
    @Singleton
    fun provideChartRenderer(
        @ApplicationContext context: Context,
    ): ChartRenderer = MpChartBitmapRenderer(context)

    @Provides
    @Singleton
    fun provideChartImageImporter(
        @ApplicationContext context: Context,
    ): ChartImageManager = ChartImageManagerImpl(context)

    @Provides
    @Singleton
    fun provideCsvService(
        weightRepository: WeightMeasureRepository,
    ): CsvService = CsvServiceImpl(weightRepository)

    @Provides
    @Singleton
    fun provideBackupService(
        @ApplicationContext context: Context,
        appSettingsService: AppSettingsService,
        weightRepository: WeightMeasureRepository,
        userProfileRepository: UserProfileRepository,
        csvService: CsvService,
        applicationScope: CoroutineScope
    ): BackupService = BackupManager(
        context,
        appSettingsService,
        weightRepository,
        userProfileRepository,
        csvService,
        applicationScope
    )

    @Provides
    @Singleton
    fun provideStorageSupport(
        @ApplicationContext context: Context,
    ): StorageSupport = StorageSupportImpl(context)

}

//@EntryPoint
//@InstallIn(SingletonComponent::class)
//interface ResourceProviderEntryPoint {
//    fun resourceProvider(): ResourceProvider
//}
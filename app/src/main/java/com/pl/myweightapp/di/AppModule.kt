package com.pl.myweightapp.di

import android.content.Context
import androidx.room.Room
import com.pl.myweightapp.data.AndroidResourceProvider
import com.pl.myweightapp.data.local.MyDatabase
import com.pl.myweightapp.data.preferences.AppSettingsDataSource
import com.pl.myweightapp.data.preferences.AppSettingsManager
import com.pl.myweightapp.data.repository.AppSettingsRepositoryImpl
import com.pl.myweightapp.data.repository.NavigationBadgeRepositoryImpl
import com.pl.myweightapp.data.repository.UserProfileRepositoryImpl
import com.pl.myweightapp.data.repository.WeightMeasureRepositoryImpl
import com.pl.myweightapp.data.chart.ChartImageExporterImpl
import com.pl.myweightapp.data.chart.ChartImageImporterImpl
import com.pl.myweightapp.data.local.BackupManager
import com.pl.myweightapp.domain.AppSettingsService
import com.pl.myweightapp.domain.BackupService
import com.pl.myweightapp.feature.home.chart.ChartRenderer
import com.pl.myweightapp.domain.NavigationBadgeRepository
import com.pl.myweightapp.domain.ResourceProvider
import com.pl.myweightapp.domain.UserProfileRepository
import com.pl.myweightapp.domain.WeightMeasureRepository
import com.pl.myweightapp.domain.chart.ChartImageExporter
import com.pl.myweightapp.domain.chart.ChartImageImporter
import com.pl.myweightapp.domain.usecase.GenerateWeightChartDataUseCase
import com.pl.myweightapp.feature.home.chart.ChartImageDecoder
import com.pl.myweightapp.feature.home.chart.ChartImageDecoderImpl
import com.pl.myweightapp.feature.home.chart.MpChartBitmapRenderer
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
        WeightMeasureRepositoryImpl(database.weightMeasureDao())

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        database: MyDatabase
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
    fun provideChartRenderer(
        @ApplicationContext context: Context,
    ): ChartRenderer = MpChartBitmapRenderer(context)

    @Provides
    @Singleton
    fun provideChartImageExporter(
        @ApplicationContext context: Context,
    ): ChartImageExporter = ChartImageExporterImpl(context)

    @Provides
    @Singleton
    fun provideChartImageImporter(
        @ApplicationContext context: Context,
    ): ChartImageImporter = ChartImageImporterImpl(context)

    @Provides
    @Singleton
    fun provideResourceProvider(//hehe, cóż za piękna nazwa ;)
        @ApplicationContext context: Context,
    ): ResourceProvider = AndroidResourceProvider(context)

    @Provides
    @Singleton
    fun provideChartImageDecoder(): ChartImageDecoder =
        ChartImageDecoderImpl()

    @Provides
    @Singleton
    fun provideBackupService(
        @ApplicationContext context: Context,
        weightRepository: WeightMeasureRepository,
        userProfileRepository: UserProfileRepository,
        applicationScope: CoroutineScope
    ): BackupService = BackupManager(
        context, weightRepository, userProfileRepository, applicationScope
    )


}

//@EntryPoint
//@InstallIn(SingletonComponent::class)
//interface ResourceProviderEntryPoint {
//    fun resourceProvider(): ResourceProvider
//}
package com.pl.myweightapp

import android.app.Application
import com.pl.myweightapp.domain.AppSettingsService
import com.pl.myweightapp.domain.BackupService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class WeightApplication: Application() {
    @Inject
    lateinit var appSettingsService: AppSettingsService

    @Suppress("unused")
    @Inject
    lateinit var backupService: BackupService

    override fun onCreate() {
        super.onCreate()
        //AppModule.initialize(this)
        //ewentualnie: CoroutineScope(Dispatchers.Default).launch {
        runBlocking {
            appSettingsService.bootstrapLang()
        }
    }
}
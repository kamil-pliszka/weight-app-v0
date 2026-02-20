package com.pl.myweightapp.app

import android.app.Application
import com.pl.myweightapp.data.preferences.AppSettingsManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application() {
    @Inject
    lateinit var appSettingsManager: AppSettingsManager

    override fun onCreate() {
        super.onCreate()
        //AppModule.initialize(this)
        //ewentualnie: CoroutineScope(Dispatchers.Default).launch {
        runBlocking {
            appSettingsManager.bootstrapLang()
        }
    }
}
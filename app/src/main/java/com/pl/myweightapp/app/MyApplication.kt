package com.pl.myweightapp.app

import android.app.Application
import com.pl.myweightapp.app.di.AppModule

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AppModule.initialize(this)
    }
}
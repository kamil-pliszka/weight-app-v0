package com.pl.myweightapp

import android.app.Application

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AppModule.initialize(this)
    }
}
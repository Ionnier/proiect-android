package com.ionnier.pdma

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class MainApplication : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
        if (BuildConfig.DEBUG) {
            Timber.plant(object: DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "PDMA (${element.fileName}:${element.lineNumber})"
                }
            })
        }
    }

}
package com.focusforge.app

import android.app.Application
import com.focusforge.app.core.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusForgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.i("Application started")
    }
}

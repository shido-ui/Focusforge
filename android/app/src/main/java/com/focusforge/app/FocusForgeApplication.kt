package com.focusforge.app

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.focusforge.app.core.AppLogger
import com.focusforge.app.sync.OutboxSyncWorker
import com.focusforge.app.work.HeartbeatWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class FocusForgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.i("Application started")
        scheduleBackgroundWork()
    }

    private fun scheduleBackgroundWork() {
        val heartbeat = PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "focusforge-session-recovery",
            ExistingPeriodicWorkPolicy.KEEP,
            heartbeat,
        )
        OutboxSyncWorker.schedule(this)
    }
}

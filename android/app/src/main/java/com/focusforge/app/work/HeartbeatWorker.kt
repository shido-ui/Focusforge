package com.focusforge.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focusforge.app.di.WorkerEntryPoint
import dagger.hilt.android.EntryPointAccessors

class HeartbeatWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        return runCatching {
            entry.focusSessionRepository().recover()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}

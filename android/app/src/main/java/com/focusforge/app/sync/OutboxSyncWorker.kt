package com.focusforge.app.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.focusforge.app.di.WorkerEntryPoint
import com.focusforge.app.network.ApiException
import dagger.hilt.android.EntryPointAccessors
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit

class OutboxSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        val db = entry.database()
        val auth = entry.authRepository()
        val api = entry.api()
        val events = db.outboxDao().pending(System.currentTimeMillis(), 50)
        if (events.isEmpty()) return Result.success()

        var retry = false
        for (event in events) {
            try {
                val token = auth.currentAccessToken()
                api.syncEvent(
                    token,
                    JSONObject()
                        .put("client_event_id", event.clientEventId)
                        .put("event_type", event.eventType)
                        .put("payload", JSONObject(event.payloadJson))
                        .put("client_created_at", Instant.ofEpochMilli(event.createdAtEpochMs).toString()),
                )
                db.outboxDao().markCompleted(event.clientEventId)
            } catch (error: ApiException) {
                val attempts = event.attemptCount + 1
                if (!error.error.retryable || attempts >= 8) {
                    db.outboxDao().markFailed(event.clientEventId)
                } else {
                    val delay = (1L shl attempts.coerceAtMost(6)) * 1000L
                    db.outboxDao().markRetry(
                        event.clientEventId,
                        attempts,
                        System.currentTimeMillis() + delay,
                    )
                    retry = true
                }
            }
        }
        db.outboxDao().purgeCompleted()
        return if (retry) Result.retry() else Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "focusforge-outbox-sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<OutboxSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}

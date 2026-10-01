package com.focusforge.app.sync

import com.focusforge.app.data.FocusForgeDatabase
import com.focusforge.app.data.OutboxEventEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutboxRepository @Inject constructor(
    private val database: FocusForgeDatabase,
) {
    private val mutex = Mutex()

    suspend fun enqueue(eventType: String, payload: JSONObject): String = mutex.withLock {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        database.outboxDao().upsert(
            OutboxEventEntity(
                clientEventId = id,
                eventType = eventType,
                payloadJson = payload.toString(),
                createdAtEpochMs = now,
                attemptCount = 0,
                nextRetryAtEpochMs = now,
                status = "PENDING",
            )
        )
        id
    }
}

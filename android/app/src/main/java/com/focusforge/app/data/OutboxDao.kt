package com.focusforge.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox_events WHERE status = 'PENDING' AND nextRetryAtEpochMs <= :now ORDER BY createdAtEpochMs LIMIT :limit")
    suspend fun pending(now: Long, limit: Int): List<OutboxEventEntity>

    @Upsert
    suspend fun upsert(event: OutboxEventEntity)

    @Query("UPDATE outbox_events SET status='COMPLETED' WHERE clientEventId=:id")
    suspend fun markCompleted(id: String)

    @Query("UPDATE outbox_events SET status='PENDING', attemptCount=:attempts, nextRetryAtEpochMs=:nextRetryAt WHERE clientEventId=:id")
    suspend fun markRetry(id: String, attempts: Int, nextRetryAt: Long)

    @Query("UPDATE outbox_events SET status='FAILED' WHERE clientEventId=:id")
    suspend fun markFailed(id: String)

    @Query("DELETE FROM outbox_events WHERE status='COMPLETED'")
    suspend fun purgeCompleted()

    @Query("DELETE FROM outbox_events")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM outbox_events WHERE status='PENDING'")
    suspend fun pendingCount(): Int
}

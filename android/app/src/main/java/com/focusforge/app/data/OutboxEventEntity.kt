package com.focusforge.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbox_events",
    indices = [Index(value = ["status", "nextRetryAtEpochMs"])]
)
data class OutboxEventEntity(
    @PrimaryKey val clientEventId: String,
    val eventType: String,
    val payloadJson: String,
    val createdAtEpochMs: Long,
    val attemptCount: Int,
    val nextRetryAtEpochMs: Long,
    val status: String,
)

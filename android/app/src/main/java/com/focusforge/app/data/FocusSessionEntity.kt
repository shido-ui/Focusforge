package com.focusforge.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FocusSessionState {
    IDLE, ARMED, LOCKED, ENDING;

    companion object {
        fun parse(value: String): FocusSessionState? = entries.firstOrNull { it.name == value }
    }
}

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val state: String,
    val startedAtEpochMs: Long?,
    val endsAtEpochMs: Long?,
    val startElapsedRealtimeMs: Long?,
    val lastElapsedRealtimeMs: Long?,
    val elapsedDurationMs: Long,
    val updatedAtEpochMs: Long,
    val revision: Long,
)

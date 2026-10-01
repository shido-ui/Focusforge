package com.focusforge.app.focus

import android.os.SystemClock
import com.focusforge.app.data.FocusSessionDao
import com.focusforge.app.data.FocusSessionEntity
import com.focusforge.app.data.FocusSessionState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionRepository @Inject constructor(
    private val dao: FocusSessionDao,
) {
    private val mutex = Mutex()

    suspend fun start(durationMs: Long): FocusSessionEntity = mutex.withLock {
        require(durationMs > 0) { "durationMs must be positive" }
        check(dao.active() == null) { "An active focus session already exists." }
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        val session = FocusSessionEntity(
            id = UUID.randomUUID().toString(),
            state = FocusSessionState.ARMED.name,
            startedAtEpochMs = nowWall,
            endsAtEpochMs = nowWall + durationMs,
            startElapsedRealtimeMs = nowElapsed,
            lastElapsedRealtimeMs = nowElapsed,
            elapsedDurationMs = 0,
            updatedAtEpochMs = nowWall,
            revision = 1,
        )
        dao.upsert(session)
        session
    }

    suspend fun transition(id: String, target: FocusSessionState): FocusSessionEntity = mutex.withLock {
        val current = dao.active()?.takeIf { it.id == id } ?: error("Focus session not found.")
        val state = FocusSessionState.parse(current.state) ?: error("Persisted session state is invalid.")
        require(isAllowed(state, target)) { "Illegal focus session transition: $state -> $target" }
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        val next = current.copy(
            state = target.name,
            lastElapsedRealtimeMs = nowElapsed,
            elapsedDurationMs = updateElapsed(current, nowElapsed, nowWall),
            updatedAtEpochMs = nowWall,
            revision = current.revision + 1,
        )
        dao.upsert(next)
        next
    }

    suspend fun recover(): FocusSessionEntity? = mutex.withLock {
        val current = dao.active() ?: return@withLock null
        val state = FocusSessionState.parse(current.state)
        if (state == null) {
            val repaired = current.copy(
                state = FocusSessionState.IDLE.name,
                updatedAtEpochMs = System.currentTimeMillis(),
                revision = current.revision + 1,
            )
            dao.upsert(repaired)
            return@withLock repaired
        }
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        val elapsed = updateElapsed(current, nowElapsed, nowWall)
        if (current.endsAtEpochMs != null && nowWall >= current.endsAtEpochMs) {
            val ending = current.copy(
                state = FocusSessionState.ENDING.name,
                lastElapsedRealtimeMs = nowElapsed,
                elapsedDurationMs = elapsed,
                updatedAtEpochMs = nowWall,
                revision = current.revision + 1,
            )
            dao.upsert(ending)
            val idle = ending.copy(
                state = FocusSessionState.IDLE.name,
                updatedAtEpochMs = nowWall,
                revision = ending.revision + 1,
            )
            dao.upsert(idle)
            return@withLock idle
        }
        val resumed = current.copy(
            lastElapsedRealtimeMs = nowElapsed,
            elapsedDurationMs = elapsed,
            updatedAtEpochMs = nowWall,
            revision = current.revision + 1,
        )
        dao.upsert(resumed)
        resumed
    }

    private fun updateElapsed(current: FocusSessionEntity, nowElapsed: Long, nowWall: Long): Long {
        val last = current.lastElapsedRealtimeMs
        val added = if (last != null && nowElapsed >= last) nowElapsed - last else 0L
        val total = current.elapsedDurationMs + added
        val wallBound = current.startedAtEpochMs?.let { (nowWall - it).coerceAtLeast(0L) } ?: total
        val durationBound = current.endsAtEpochMs?.let { end ->
            current.startedAtEpochMs?.let { (end - it).coerceAtLeast(0L) }
        }
        return minOf(total, wallBound, durationBound ?: Long.MAX_VALUE)
    }

    private fun isAllowed(from: FocusSessionState, to: FocusSessionState): Boolean = when (from) {
        FocusSessionState.IDLE -> to == FocusSessionState.ARMED
        FocusSessionState.ARMED -> to == FocusSessionState.LOCKED || to == FocusSessionState.ENDING
        FocusSessionState.LOCKED -> to == FocusSessionState.ENDING
        FocusSessionState.ENDING -> to == FocusSessionState.IDLE
    }
}

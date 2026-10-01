package com.focusforge.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions WHERE id=:id LIMIT 1")
    fun observe(id: String): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE state != 'IDLE' ORDER BY updatedAtEpochMs DESC LIMIT 1")
    suspend fun active(): FocusSessionEntity?

    @Upsert
    suspend fun upsert(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions WHERE id=:id")
    suspend fun delete(id: String)

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM focus_sessions")
    suspend fun count(): Int
}

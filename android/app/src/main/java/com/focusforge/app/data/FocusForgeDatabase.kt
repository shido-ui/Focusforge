package com.focusforge.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FocusSessionEntity::class, OutboxEventEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class FocusForgeDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN startElapsedRealtimeMs INTEGER")
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN lastElapsedRealtimeMs INTEGER")
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN elapsedDurationMs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS outbox_events (
                        clientEventId TEXT NOT NULL,
                        eventType TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        createdAtEpochMs INTEGER NOT NULL,
                        attemptCount INTEGER NOT NULL,
                        nextRetryAtEpochMs INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        PRIMARY KEY(clientEventId)
                    )
                """.trimIndent())
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_outbox_events_status_nextRetryAtEpochMs " +
                        "ON outbox_events(status, nextRetryAtEpochMs)"
                )
            }
        }
    }
}

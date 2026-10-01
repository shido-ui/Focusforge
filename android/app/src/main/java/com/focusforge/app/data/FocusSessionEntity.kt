package com.focusforge.app.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="focus_sessions")
data class FocusSessionEntity(
 @PrimaryKey val id:String,
 val state:String,
 val startedAtEpochMs:Long?,
 val endsAtEpochMs:Long?,
 val updatedAtEpochMs:Long
)

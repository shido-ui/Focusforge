package com.focusforge.app.data
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities=[FocusSessionEntity::class],version=1,exportSchema=true)
abstract class FocusForgeDatabase:RoomDatabase(){ abstract fun focusSessionDao():FocusSessionDao }

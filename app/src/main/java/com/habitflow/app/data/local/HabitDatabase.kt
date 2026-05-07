package com.habitflow.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, HabitRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}

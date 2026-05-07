package com.habitflow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("UPDATE habits SET isArchived = :isArchived WHERE id = :habitId")
    suspend fun updateArchiveStatus(habitId: Long, isArchived: Boolean)

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY date DESC")
    fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecordEntity>>

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getRecordForDate(habitId: Long, date: String): HabitRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HabitRecordEntity)

    @Query("DELETE FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun deleteRecord(habitId: Long, date: String)

    @Query("SELECT COUNT(*) FROM habit_records WHERE habitId = :habitId")
    fun getCompletedDaysCount(habitId: Long): Flow<Int>

    @Query("SELECT * FROM habit_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<HabitRecordEntity>>
}

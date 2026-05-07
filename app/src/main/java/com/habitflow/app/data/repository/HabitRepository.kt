package com.habitflow.app.data.repository

import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitRecord
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getHabitById(habitId: Long): Habit?
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: Long)
    suspend fun toggleHabitCompletion(habitId: Long, date: String): Boolean
    fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecord>>
    fun getCompletedDaysCount(habitId: Long): Flow<Int>
    fun getRecordsForDate(date: String): Flow<List<HabitRecord>>
}

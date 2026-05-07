package com.habitflow.app.data.repository

import com.habitflow.app.data.local.HabitDao
import com.habitflow.app.data.local.HabitEntity
import com.habitflow.app.data.local.HabitRecordEntity
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { entity ->
                entity.toHabit()
            }
        }
    }

    override suspend fun getHabitById(habitId: Long): Habit? {
        return habitDao.getHabitById(habitId)?.toHabit()
    }

    override suspend fun addHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit.toEntity())
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.toEntity())
    }

    override suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
    }

    override suspend fun toggleHabitCompletion(habitId: Long, date: String): Boolean {
        val existingRecord = habitDao.getRecordForDate(habitId, date)
        return if (existingRecord != null) {
            habitDao.deleteRecord(habitId, date)
            false
        } else {
            habitDao.insertRecord(HabitRecordEntity(habitId = habitId, date = date))
            true
        }
    }

    override fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecord>> {
        return habitDao.getRecordsForHabit(habitId).map { entities ->
            entities.map { it.toRecord() }
        }
    }

    override fun getCompletedDaysCount(habitId: Long): Flow<Int> {
        return habitDao.getCompletedDaysCount(habitId)
    }

    override fun getRecordsForDate(date: String): Flow<List<HabitRecord>> {
        return habitDao.getRecordsForDate(date).map { entities ->
            entities.map { it.toRecord() }
        }
    }

    private fun HabitEntity.toHabit(): Habit {
        return Habit(
            id = id,
            name = name,
            description = description,
            colorHex = colorHex,
            targetDays = targetDays,
            createdAt = createdAt,
            isArchived = isArchived
        )
    }

    private fun Habit.toEntity(): HabitEntity {
        return HabitEntity(
            id = id,
            name = name,
            description = description,
            colorHex = colorHex,
            targetDays = targetDays,
            createdAt = createdAt,
            isArchived = isArchived
        )
    }

    private fun HabitRecordEntity.toRecord(): HabitRecord {
        return HabitRecord(
            id = id,
            habitId = habitId,
            date = date,
            completedAt = completedAt
        )
    }
}

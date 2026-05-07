package com.habitflow.app.data.model

data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String,
    val colorHex: String,
    val targetDays: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val completedDays: Int = 0
)

data class HabitRecord(
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val completedAt: Long = System.currentTimeMillis()
)

package com.habitflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val colorHex: String,
    val targetDays: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

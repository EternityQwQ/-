package com.habitflow.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitRecord
import com.habitflow.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HabitDetailUiState(
    val habit: Habit? = null,
    val records: List<HabitRecord> = emptyList(),
    val completedDays: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val progressPercentage: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val repository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: 0

    val uiState: StateFlow<HabitDetailUiState> = combine(
        repository.getAllHabits(),
        repository.getRecordsForHabit(habitId)
    ) { habits, records ->
        val habit = habits.find { it.id == habitId }
        val completedDays = records.size
        val dates = records.map { LocalDate.parse(it.date) }.sortedDescending()

        val currentStreak = calculateCurrentStreak(dates)
        val longestStreak = calculateLongestStreak(dates)

        val progress = if (habit != null && habit.targetDays > 0) {
            (completedDays.toFloat() / habit.targetDays).coerceAtMost(1f)
        } else 0f

        HabitDetailUiState(
            habit = habit,
            records = records,
            completedDays = completedDays,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            progressPercentage = progress,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitDetailUiState()
    )

    fun toggleTodayCompletion() {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, getCurrentDateString())
        }
    }

    fun deleteHabit() {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    private fun calculateCurrentStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.sortedDescending()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        if (sortedDates.first() != today && sortedDates.first() != yesterday) return 0

        var streak = 1
        var currentDate = sortedDates.first()

        for (i in 1 until sortedDates.size) {
            val previousDate = sortedDates[i]
            if (ChronoUnit.DAYS.between(previousDate, currentDate) == 1L) {
                streak++
                currentDate = previousDate
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.sortedDescending()
        var longestStreak = 1
        var currentStreak = 1
        var previousDate = sortedDates.first()

        for (i in 1 until sortedDates.size) {
            val currentDate = sortedDates[i]
            if (ChronoUnit.DAYS.between(currentDate, previousDate) == 1L) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
            previousDate = currentDate
        }
        return longestStreak
    }

    private fun getCurrentDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

package com.habitflow.app.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitRecord
import com.habitflow.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HabitWithRecord(
    val habit: Habit,
    val isCompletedToday: Boolean
)

data class HabitListUiState(
    val habits: List<HabitWithRecord> = emptyList(),
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now()
)

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HabitListUiState> = combine(
        repository.getAllHabits(),
        repository.getRecordsForDate(getCurrentDateString()),
        _selectedDate
    ) { habits, todayRecords, selectedDate ->
        val todayRecordsSet = todayRecords.map { it.habitId }.toSet()
        HabitListUiState(
            habits = habits.map { habit ->
                HabitWithRecord(
                    habit = habit,
                    isCompletedToday = habit.id in todayRecordsSet
                )
            },
            isLoading = false,
            selectedDate = selectedDate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitListUiState()
    )

    fun toggleHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, getCurrentDateString())
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    private fun getCurrentDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

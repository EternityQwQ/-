package com.habitflow.app.ui.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.ui.theme.habitColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditHabitUiState(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val selectedColor: String = habitColors.first(),
    val targetDays: Int = 30,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null
)

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: 0

    private val _uiState = MutableStateFlow(AddEditHabitUiState())
    val uiState: StateFlow<AddEditHabitUiState> = _uiState.asStateFlow()

    init {
        if (habitId > 0) {
            loadHabit()
        }
    }

    private fun loadHabit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getHabitById(habitId)?.let { habit ->
                _uiState.update {
                    it.copy(
                        id = habit.id,
                        name = habit.name,
                        description = habit.description,
                        selectedColor = habit.colorHex,
                        targetDays = habit.targetDays,
                        isEditing = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun updateTargetDays(days: Int) {
        _uiState.update { it.copy(targetDays = days.coerceIn(1, 365)) }
    }

    fun saveHabit() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "习惯名称不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val habit = Habit(
                id = currentState.id,
                name = currentState.name.trim(),
                description = currentState.description.trim(),
                colorHex = currentState.selectedColor,
                targetDays = currentState.targetDays
            )

            if (currentState.isEditing) {
                repository.updateHabit(habit)
            } else {
                repository.addHabit(habit)
            }

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}

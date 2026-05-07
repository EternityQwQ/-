package com.thermalfaker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalfaker.app.core.shizuku.ShizukuManager
import com.thermalfaker.app.core.shizuku.ShizukuStatus
import com.thermalfaker.app.core.shizuku.TemperatureManager
import com.thermalfaker.app.data.model.TemperatureSettings
import com.thermalfaker.app.data.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val shizukuManager: ShizukuManager,
    private val temperatureManager: TemperatureManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(TemperatureSettings())
    val settings: StateFlow<TemperatureSettings> = _settings.asStateFlow()

    private val _shizukuStatus = MutableStateFlow<ShizukuStatus>(ShizukuStatus.Unavailable)
    val shizukuStatus: StateFlow<ShizukuStatus> = _shizukuStatus.asStateFlow()

    private val _currentTemp = MutableStateFlow(0)
    val currentTemp: StateFlow<Int> = _currentTemp.asStateFlow()

    init {
        shizukuManager.status.onEach { status ->
            _shizukuStatus.value = status
        }.launchIn(viewModelScope)

        refreshTemperature()
    }

    fun updateTargetTemp(temp: Int) {
        _settings.value = _settings.value.copy(targetBatteryTemp = temp)
    }

    fun applySpoofing() {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)
            val result = temperatureManager.setBatteryTemperature(_settings.value.targetBatteryTemp)
            if (result.startsWith("Error")) {
                _uiState.value = UiState(errorMessage = result)
            } else {
                _settings.value = _settings.value.copy(isBatterySpoofingActive = true)
                _uiState.value = UiState(successMessage = "Temperature spoofing applied successfully")
            }
        }
    }

    fun resetTemperature() {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)
            val result = temperatureManager.resetBatteryTemperature()
            if (result.startsWith("Error")) {
                _uiState.value = UiState(errorMessage = result)
            } else {
                _settings.value = _settings.value.copy(isBatterySpoofingActive = false)
                _uiState.value = UiState(successMessage = "Temperature reset to real value")
            }
        }
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun refreshTemperature() {
        _currentTemp.value = temperatureManager.getCurrentBatteryTemperature()
    }

    fun clearMessages() {
        _uiState.value = UiState()
    }
}

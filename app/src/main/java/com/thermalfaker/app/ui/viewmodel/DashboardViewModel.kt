package com.thermalfaker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalfaker.app.core.shizuku.HardwareMonitor
import com.thermalfaker.app.core.shizuku.TemperatureManager
import com.thermalfaker.app.data.model.DashboardState
import com.thermalfaker.app.data.model.HardwareType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val hardwareMonitor: HardwareMonitor,
    private val temperatureManager: TemperatureManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hardwareMonitor.hardwareInfo.collect { info ->
                _uiState.update { currentState ->
                    currentState.copy(
                        hardwareInfo = info,
                        isLoading = false
                    )
                }
            }
        }

        refreshHardwareInfo()
    }

    fun refreshHardwareInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            hardwareMonitor.refreshHardwareInfo()
                .onSuccess { info ->
                    _uiState.update {
                        it.copy(
                            hardwareInfo = info,
                            isLoading = false,
                            lastRefreshTime = System.currentTimeMillis()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unknown error"
                        )
                    }
                }
        }
    }

    fun setGlobalTemperature(temperature: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            temperatureManager.setGlobalTemperature(temperature)
                .onSuccess { results ->
                    val allSuccess = results.values.all { it == "Success" }
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            globalTemperature = temperature,
                            spoofStatus = temperatureManager.getAllSpoofingStatus(),
                            successMessage = if (allSuccess) {
                                "Global temperature set to $temperature°C"
                            } else {
                                "Partial success: ${results.filter { it.value == "Success" }.keys.joinToString()}"
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to set global temperature"
                        )
                    }
                }
        }
    }

    fun setHardwareTemperature(type: HardwareType, temperature: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = when (type) {
                HardwareType.BATTERY -> {
                    val res = temperatureManager.setBatteryTemperature(temperature)
                    if (res.contains("Error")) Result.failure(Exception(res)) else Result.success(res)
                }
                HardwareType.CPU -> temperatureManager.setCpuTemperature(temperature)
                HardwareType.GPU -> temperatureManager.setGpuTemperature(temperature)
                HardwareType.AMBIENT -> temperatureManager.setAmbientTemperature(temperature)
            }

            result
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            spoofStatus = temperatureManager.getAllSpoofingStatus(),
                            successMessage = message
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to set ${type.name} temperature"
                        )
                    }
                }
        }
    }

    fun resetHardwareTemperature(type: HardwareType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            temperatureManager.resetHardwareTemperature(type)
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            spoofStatus = temperatureManager.getAllSpoofingStatus(),
                            successMessage = message
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to reset ${type.name} temperature"
                        )
                    }
                }
        }
    }

    fun resetAllTemperatures() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            temperatureManager.resetAllTemperatures()
                .onSuccess { results ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            globalTemperature = null,
                            spoofStatus = temperatureManager.getAllSpoofingStatus(),
                            successMessage = "All temperatures reset to real values"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to reset temperatures"
                        )
                    }
                }
        }
    }

    fun updateGlobalTemperatureInput(value: String) {
        val temp = value.toIntOrNull()
        _uiState.update { it.copy(globalTemperatureInput = temp) }
    }

    fun updateHardwareTemperatureInput(type: HardwareType, value: String) {
        val temp = value.toIntOrNull()
        _uiState.update { currentState ->
            val newMap = currentState.hardwareTemperatureInputs.toMutableMap()
            newMap[type] = temp
            currentState.copy(hardwareTemperatureInputs = newMap)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun isSpoofingActive(type: HardwareType): Boolean {
        return temperatureManager.isSpoofingActive(type)
    }

    fun getSpoofedTemperature(type: HardwareType): Int? {
        return temperatureManager.getSpoofedTemperature(type)
    }
}

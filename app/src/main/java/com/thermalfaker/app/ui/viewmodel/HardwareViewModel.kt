package com.thermalfaker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalfaker.app.core.shizuku.HardwareManager
import com.thermalfaker.app.core.shizuku.ShizukuManager
import com.thermalfaker.app.core.shizuku.ShizukuStatus
import com.thermalfaker.app.core.shizuku.TemperatureManager
import com.thermalfaker.app.data.model.HardwareInfo
import com.thermalfaker.app.data.model.TemperatureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HardwareViewModel @Inject constructor(
    private val hardwareManager: HardwareManager,
    private val temperatureManager: TemperatureManager,
    private val shizukuManager: ShizukuManager
) : ViewModel() {

    private val _hardwareInfo = MutableStateFlow(HardwareInfo())
    val hardwareInfo: StateFlow<HardwareInfo> = _hardwareInfo.asStateFlow()

    private val _shizukuStatus = MutableStateFlow<ShizukuStatus>(ShizukuStatus.Unavailable)
    val shizukuStatus: StateFlow<ShizukuStatus> = _shizukuStatus.asStateFlow()

    private val _cpuSpoofTemp = MutableStateFlow(35)
    val cpuSpoofTemp: StateFlow<Int> = _cpuSpoofTemp.asStateFlow()

    private val _gpuSpoofTemp = MutableStateFlow(35)
    val gpuSpoofTemp: StateFlow<Int> = _gpuSpoofTemp.asStateFlow()

    private val _batterySpoofTemp = MutableStateFlow(35)
    val batterySpoofTemp: StateFlow<Int> = _batterySpoofTemp.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    init {
        shizukuManager.status.onEach { status ->
            _shizukuStatus.value = status
        }.launchIn(viewModelScope)

        refreshHardwareInfo()
    }

    fun refreshHardwareInfo() {
        viewModelScope.launch {
            _hardwareInfo.value = _hardwareInfo.value.copy(isRefreshing = true)
            _hardwareInfo.value = hardwareManager.getHardwareInfo()
        }
    }

    fun updateCpuTemp(temp: Int) {
        _cpuSpoofTemp.value = temp
    }

    fun updateGpuTemp(temp: Int) {
        _gpuSpoofTemp.value = temp
    }

    fun updateBatteryTemp(temp: Int) {
        _batterySpoofTemp.value = temp
    }

    fun applyAllTemps() {
        viewModelScope.launch {
            _isApplying.value = true
            
            val results = mutableListOf<String>()
            
            if (_hardwareInfo.value.cpuTemp.supported) {
                val result = hardwareManager.setCpuTemperature(_cpuSpoofTemp.value)
                results.add(result)
            }
            
            if (_hardwareInfo.value.gpuTemp.supported) {
                val result = hardwareManager.setGpuTemperature(_gpuSpoofTemp.value)
                results.add(result)
            }
            
            val batteryResult = temperatureManager.setBatteryTemperature(_batterySpoofTemp.value)
            results.add(batteryResult)
            
            _message.value = results.joinToString("\n")
            _isApplying.value = false
            
            refreshHardwareInfo()
            
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _message.value = null
            }
        }
    }

    fun resetAllTemps() {
        viewModelScope.launch {
            _isApplying.value = true
            val result = hardwareManager.resetAllTemperatures()
            _message.value = result
            _isApplying.value = false
            
            refreshHardwareInfo()
            
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _message.value = null
            }
        }
    }

    fun setGlobalTemp(temp: Int) {
        _cpuSpoofTemp.value = temp
        _gpuSpoofTemp.value = temp
        _batterySpoofTemp.value = temp
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }
}

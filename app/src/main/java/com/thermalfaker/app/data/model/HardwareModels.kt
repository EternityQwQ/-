package com.thermalfaker.app.data.model

data class HardwareInfo(
    val cpuTemperature: TempInfo = TempInfo(),
    val gpuTemperature: TempInfo = TempInfo(),
    val batteryTemperature: TempInfo = TempInfo(),
    val cpuFrequency: FreqInfo = FreqInfo(),
    val gpuFrequency: FreqInfo = FreqInfo(),
    val cpuCoreCount: Int = 0,
    val cpuUsage: Float = 0f
)

data class TempInfo(
    val realValue: Float = 0f,
    val spoofedValue: Float = 0f,
    val isSpoofed: Boolean = false,
    val isSupported: Boolean = true,
    val sensorName: String = ""
)

data class FreqInfo(
    val current: Long = 0,
    val min: Long = 0,
    val max: Long = 0,
    val isSupported: Boolean = true
)

data class MultiTempSettings(
    val cpuTargetTemp: Int = 30,
    val gpuTargetTemp: Int = 30,
    val batteryTargetTemp: Int = 30,
    val isCpuSpoofing: Boolean = false,
    val isGpuSpoofing: Boolean = false,
    val isBatterySpoofing: Boolean = false
)

data class HardwareUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
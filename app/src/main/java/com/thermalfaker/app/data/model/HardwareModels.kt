package com.thermalfaker.app.data.model

data class HardwareInfo(
    val cpuTemp: Int? = null,
    val cpuTempSupported: Boolean = false,
    val cpuTempError: String? = null,
    val batteryTemp: Int? = null,
    val batteryTempSupported: Boolean = true,
    val batteryTempError: String? = null,
    val gpuTemp: Int? = null,
    val gpuTempSupported: Boolean = false,
    val gpuTempError: String? = null,
    val boardTemp: Int? = null,
    val boardTempSupported: Boolean = false,
    val boardTempError: String? = null,
    val cpuFrequency: List<CpuCoreInfo> = emptyList(),
    val gpuFrequency: Int? = null,
    val gpuFrequencySupported: Boolean = false,
    val thermalZones: List<ThermalZoneInfo> = emptyList(),
    val lastUpdateTime: Long = System.currentTimeMillis()
)

data class CpuCoreInfo(
    val coreIndex: Int,
    val currentFreqMHz: Int,
    val minFreqMHz: Int,
    val maxFreqMHz: Int
)

data class ThermalZoneInfo(
    val name: String,
    val path: String,
    val temperature: Int,
    val type: String
)

data class SpoofingConfig(
    val isEnabled: Boolean = false,
    val targetTemp: Int = 25
)

data class MultiTemperatureSettings(
    val globalSpoofEnabled: Boolean = false,
    val globalTargetTemp: Int = 25,
    val cpuSpoof: SpoofingConfig = SpoofingConfig(),
    val batterySpoof: SpoofingConfig = SpoofingConfig(),
    val gpuSpoof: SpoofingConfig = SpoofingConfig(),
    val boardSpoof: SpoofingConfig = SpoofingConfig()
)

data class CommandResult(
    val success: Boolean,
    val output: String = "",
    val error: String = ""
)

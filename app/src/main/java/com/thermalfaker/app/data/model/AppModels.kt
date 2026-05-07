package com.thermalfaker.app.data.model

import com.thermalfaker.app.data.model.HardwareType

data class TemperatureSettings(
    val targetBatteryTemp: Int = 30,
    val isBatterySpoofingActive: Boolean = false,
    val targetCpuTemp: Int = 40,
    val isCpuSpoofingActive: Boolean = false,
    val targetGpuTemp: Int = 45,
    val isGpuSpoofingActive: Boolean = false,
    val targetAmbientTemp: Int = 25,
    val isAmbientSpoofingActive: Boolean = false,
    val useGlobalTemp: Boolean = false,
    val globalTemp: Int = 35
)

data class UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class HardwareInfo(
    val batteryTemp: Int = 0,
    val cpuTemp: Int = 0,
    val gpuTemp: Int = 0,
    val ambientTemp: Int = 0,
    val cpuFrequencies: List<CpuCoreInfo> = emptyList(),
    val gpuFrequency: Int = 0,
    val isCpuSupported: Boolean = true,
    val isGpuSupported: Boolean = true,
    val isAmbientSupported: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class CpuCoreInfo(
    val coreId: Int,
    val frequencyMHz: Int,
    val maxFrequencyMHz: Int,
    val isOnline: Boolean
)

enum class HardwareType {
    BATTERY, CPU, GPU, AMBIENT
}

data class SpoofStatus(
    val type: HardwareType,
    val isActive: Boolean,
    val spoofedValue: Int,
    val realValue: Int
)

data class DashboardState(
    val hardwareInfo: HardwareInfo = HardwareInfo(),
    val spoofStatuses: List<SpoofStatus> = emptyList(),
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

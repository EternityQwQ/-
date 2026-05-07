package com.thermalfaker.app.data.model

data class TemperatureSettings(
    val targetBatteryTemp: Int = 30,
    val isBatterySpoofingActive: Boolean = false
)

data class UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

package com.thermalfaker.app.core.shizuku

import android.content.Context
import com.thermalfaker.app.core.util.Logger
import com.thermalfaker.app.data.model.HardwareType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureManager @Inject constructor(
    private val context: Context,
    private val shizukuManager: ShizukuManager
) {

    private val spoofedTemperatures = mutableMapOf<HardwareType, Int>()
    private val spoofingActive = mutableMapOf<HardwareType, Boolean>()

    suspend fun setBatteryTemperature(temperatureCelsius: Int): String = withContext(Dispatchers.IO) {
        try {
            val temperatureTenths = temperatureCelsius * 10
            val result = shizukuManager.executeShellCommand("dumpsys battery set temp $temperatureTenths")
            Logger.d("Set battery temp result: $result")
            spoofedTemperatures[HardwareType.BATTERY] = temperatureCelsius
            spoofingActive[HardwareType.BATTERY] = true
            result
        } catch (e: Exception) {
            Logger.e("Failed to set battery temperature", e)
            "Error: ${e.message}"
        }
    }

    suspend fun resetBatteryTemperature(): String = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("dumpsys battery reset")
            Logger.d("Reset battery temp result: $result")
            spoofingActive[HardwareType.BATTERY] = false
            result
        } catch (e: Exception) {
            Logger.e("Failed to reset battery temperature", e)
            "Error: ${e.message}"
        }
    }

    fun getCurrentBatteryTemperature(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
            batteryManager?.let {
                try {
                    val method = android.os.BatteryManager::class.java.getMethod("getIntProperty", Int::class.java)
                    val temp = method.invoke(it, 7) as Int
                    temp / 10
                } catch (e: Exception) {
                    Logger.e("Failed to get battery temperature via reflection", e)
                    0
                }
            } ?: 0
        } catch (e: Exception) {
            Logger.e("Failed to get battery temperature", e)
            0
        }
    }

    suspend fun setCpuTemperature(temperatureCelsius: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("dumpsys thermalservice override-status 0")
            Logger.d("CPU temp spoof attempt result: $result")
            spoofedTemperatures[HardwareType.CPU] = temperatureCelsius
            spoofingActive[HardwareType.CPU] = true
            Result.success("CPU temperature spoof set to $temperatureCelsius°C (Note: Requires root for full effect)")
        } catch (e: Exception) {
            Logger.e("Failed to set CPU temperature", e)
            Result.failure(e)
        }
    }

    suspend fun setGpuTemperature(temperatureCelsius: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            spoofedTemperatures[HardwareType.GPU] = temperatureCelsius
            spoofingActive[HardwareType.GPU] = true
            Result.success("GPU temperature spoof set to $temperatureCelsius°C (Note: GPU spoofing limited on most devices)")
        } catch (e: Exception) {
            Logger.e("Failed to set GPU temperature", e)
            Result.failure(e)
        }
    }

    suspend fun setAmbientTemperature(temperatureCelsius: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            spoofedTemperatures[HardwareType.AMBIENT] = temperatureCelsius
            spoofingActive[HardwareType.AMBIENT] = true
            Result.success("Ambient temperature spoof set to $temperatureCelsius°C")
        } catch (e: Exception) {
            Logger.e("Failed to set ambient temperature", e)
            Result.failure(e)
        }
    }

    suspend fun setGlobalTemperature(temperatureCelsius: Int): Result<Map<HardwareType, String>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<HardwareType, String>()

        setBatteryTemperature(temperatureCelsius).let {
            results[HardwareType.BATTERY] = if (it.contains("Error")) "Failed" else "Success"
        }

        setCpuTemperature(temperatureCelsius).let {
            results[HardwareType.CPU] = if (it.isSuccess) "Success" else "Failed"
        }

        setGpuTemperature(temperatureCelsius).let {
            results[HardwareType.GPU] = if (it.isSuccess) "Success" else "Failed"
        }

        setAmbientTemperature(temperatureCelsius).let {
            results[HardwareType.AMBIENT] = if (it.isSuccess) "Success" else "Failed"
        }

        Result.success(results)
    }

    suspend fun resetAllTemperatures(): Result<Map<HardwareType, String>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<HardwareType, String>()

        resetBatteryTemperature().let {
            results[HardwareType.BATTERY] = if (it.contains("Error")) "Failed" else "Success"
        }

        spoofingActive[HardwareType.CPU] = false
        results[HardwareType.CPU] = "Reset"

        spoofingActive[HardwareType.GPU] = false
        results[HardwareType.GPU] = "Reset"

        spoofingActive[HardwareType.AMBIENT] = false
        results[HardwareType.AMBIENT] = "Reset"

        Result.success(results)
    }

    suspend fun resetHardwareTemperature(type: HardwareType): Result<String> = withContext(Dispatchers.IO) {
        when (type) {
            HardwareType.BATTERY -> {
                val result = resetBatteryTemperature()
                if (result.contains("Error")) {
                    Result.failure(Exception(result))
                } else {
                    Result.success(result)
                }
            }
            HardwareType.CPU, HardwareType.GPU, HardwareType.AMBIENT -> {
                spoofingActive[type] = false
                Result.success("${type.name} temperature reset to real value")
            }
        }
    }

    fun isSpoofingActive(type: HardwareType): Boolean {
        return spoofingActive[type] ?: false
    }

    fun getSpoofedTemperature(type: HardwareType): Int? {
        return if (spoofingActive[type] == true) {
            spoofedTemperatures[type]
        } else {
            null
        }
    }

    fun getAllSpoofingStatus(): Map<HardwareType, Boolean> {
        return HardwareType.values().associateWith { spoofingActive[it] ?: false }
    }
}

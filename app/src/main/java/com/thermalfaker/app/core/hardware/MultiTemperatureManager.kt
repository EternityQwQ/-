package com.thermalfaker.app.core.hardware

import com.thermalfaker.app.core.shizuku.ShizukuManager
import com.thermalfaker.app.core.util.Logger
import com.thermalfaker.app.data.model.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiTemperatureManager @Inject constructor(
    private val shizukuManager: ShizukuManager
) {
    suspend fun setBatteryTemperature(temperatureCelsius: Int): CommandResult = withContext(Dispatchers.IO) {
        try {
            val temperatureTenths = temperatureCelsius * 10
            val result = shizukuManager.executeShellCommand("dumpsys battery set temp $temperatureTenths")
            Logger.d("Battery temp set result: $result")
            CommandResult(success = result.isEmpty() || !result.contains("error", ignoreCase = true))
        } catch (e: Exception) {
            Logger.e("Failed to set battery temperature", e)
            CommandResult(success = false, error = e.message ?: "Unknown error")
        }
    }

    suspend fun setCpuTemperature(temperatureCelsius: Int): CommandResult = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
            )
            var success = false
            for (path in paths) {
                try {
                    val cmd = "echo ${temperatureCelsius * 1000} > $path"
                    val result = shizukuManager.executeShellCommand(cmd)
                    if (result.isEmpty()) {
                        success = true
                        break
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            if (!success) {
                val result = shizukuManager.executeShellCommand("dumpsys thermalservice override cpu ${temperatureCelsius * 1000}")
                success = !result.contains("error", ignoreCase = true)
            }
            CommandResult(success = success)
        } catch (e: Exception) {
            Logger.e("Failed to set CPU temperature", e)
            CommandResult(success = false, error = "Root required or device not supported")
        }
    }

    suspend fun setGpuTemperature(temperatureCelsius: Int): CommandResult = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/thermal/thermal_zone2/temp",
                "/sys/devices/platform/gs101-tmu-v2/sensor-TMU-v2/temperature"
            )
            var success = false
            for (path in paths) {
                try {
                    val cmd = "echo ${temperatureCelsius * 1000} > $path"
                    val result = shizukuManager.executeShellCommand(cmd)
                    if (result.isEmpty()) {
                        success = true
                        break
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            CommandResult(success = success, error = if (!success) "Device not supported" else "")
        } catch (e: Exception) {
            Logger.e("Failed to set GPU temperature", e)
            CommandResult(success = false, error = "Root required or device not supported")
        }
    }

    suspend fun setBoardTemperature(temperatureCelsius: Int): CommandResult = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp"
            )
            var success = false
            for (path in paths) {
                try {
                    val cmd = "echo ${temperatureCelsius * 1000} > $path"
                    val result = shizukuManager.executeShellCommand(cmd)
                    if (result.isEmpty()) {
                        success = true
                        break
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            CommandResult(success = success, error = if (!success) "Device not supported" else "")
        } catch (e: Exception) {
            Logger.e("Failed to set board temperature", e)
            CommandResult(success = false, error = "Root required or device not supported")
        }
    }

    suspend fun setGlobalTemperature(temperatureCelsius: Int): List<CommandResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<CommandResult>()
        results.add(setBatteryTemperature(temperatureCelsius))
        results.add(setCpuTemperature(temperatureCelsius))
        results.add(setGpuTemperature(temperatureCelsius))
        results.add(setBoardTemperature(temperatureCelsius))
        results
    }

    suspend fun resetAllTemperatures(): CommandResult = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("dumpsys battery reset")
            Logger.d("Reset all temps result: $result")
            
            val paths = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/thermal/thermal_zone2/temp"
            )
            for (path in paths) {
                try {
                    shizukuManager.executeShellCommand("rmmod thermal 2>/dev/null; modprobe thermal 2>/dev/null")
                } catch (e: Exception) {
                    continue
                }
            }
            
            CommandResult(success = true)
        } catch (e: Exception) {
            Logger.e("Failed to reset temperatures", e)
            CommandResult(success = false, error = e.message ?: "Unknown error")
        }
    }

    suspend fun resetBatteryTemperature(): CommandResult = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("dumpsys battery reset")
            CommandResult(success = true)
        } catch (e: Exception) {
            Logger.e("Failed to reset battery temperature", e)
            CommandResult(success = false, error = e.message ?: "Unknown error")
        }
    }

    suspend fun checkRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("id")
            result.contains("uid=0") || result.contains("root")
        } catch (e: Exception) {
            false
        }
    }
}

package com.thermalfaker.app.core.shizuku

import android.content.Context
import android.os.BatteryManager
import com.thermalfaker.app.core.util.Logger
import com.thermalfaker.app.data.model.FreqInfo
import com.thermalfaker.app.data.model.HardwareInfo
import com.thermalfaker.app.data.model.TempInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizukuManager: ShizukuManager
) {

    suspend fun getHardwareInfo(): HardwareInfo = withContext(Dispatchers.IO) {
        try {
            val batteryLevel = getBatteryLevel()
            val batteryTemp = getBatteryTempInfo()
            val cpuTemp = getCpuTempInfo()
            val gpuTemp = getGpuTempInfo()
            val cpuFreq = getCpuFreqInfo()
            val gpuFreq = getGpuFreqInfo()
            val cpuUsage = getCpuUsage()

            HardwareInfo(
                cpuTemp = cpuTemp,
                gpuTemp = gpuTemp,
                batteryTemp = batteryTemp,
                cpuFreq = cpuFreq,
                gpuFreq = gpuFreq,
                cpuUsage = cpuUsage,
                batteryLevel = batteryLevel,
                isRefreshing = false
            )
        } catch (e: Exception) {
            Logger.e("Failed to get hardware info", e)
            HardwareInfo(isRefreshing = false)
        }
    }

    private fun getBatteryLevel(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun getBatteryTempInfo(): TempInfo {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val temp = batteryManager?.run {
                javaClass.getMethod("getIntProperty", Int::class.java)
                    .invoke(this, 7) as? Int
            } ?: 0
            TempInfo(realTemp = temp / 10, supported = true)
        } catch (e: Exception) {
            TempInfo(supported = false, error = "Not supported")
        }
    }

    private suspend fun getCpuTempInfo(): TempInfo = withContext(Dispatchers.IO) {
        return@withContext try {
            val thermalZones = File("/sys/class/thermal").listFiles()?.filter {
                it.name.startsWith("thermal_zone")
            } ?: emptyList()

            var maxTemp = 0
            for (zone in thermalZones) {
                try {
                    val typeFile = File(zone, "type")
                    if (typeFile.exists()) {
                        val type = typeFile.readText().trim()
                        if (type.contains("cpu", ignoreCase = true) || type.contains("big", ignoreCase = true)) {
                            val tempFile = File(zone, "temp")
                            if (tempFile.exists()) {
                                val temp = tempFile.readText().trim().toIntOrNull() ?: 0
                                if (temp > maxTemp && temp < 100000) {
                                    maxTemp = temp / 1000
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            if (maxTemp > 0) {
                TempInfo(realTemp = maxTemp, supported = true)
            } else {
                val result = shizukuManager.executeShellCommand("cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null | head -1")
                val temp = result.trim().toIntOrNull() ?: 0
                if (temp > 0 && temp < 100000) {
                    TempInfo(realTemp = temp / 1000, supported = true)
                } else {
                    TempInfo(supported = false, error = "Cannot read CPU temp")
                }
            }
        } catch (e: Exception) {
            TempInfo(supported = false, error = e.message)
        }
    }

    private suspend fun getGpuTempInfo(): TempInfo = withContext(Dispatchers.IO) {
        return@withContext try {
            val thermalZones = File("/sys/class/thermal").listFiles()?.filter {
                it.name.startsWith("thermal_zone")
            } ?: emptyList()

            for (zone in thermalZones) {
                try {
                    val typeFile = File(zone, "type")
                    if (typeFile.exists()) {
                        val type = typeFile.readText().trim()
                        if (type.contains("gpu", ignoreCase = true)) {
                            val tempFile = File(zone, "temp")
                            if (tempFile.exists()) {
                                val temp = tempFile.readText().trim().toIntOrNull() ?: 0
                                if (temp > 0 && temp < 100000) {
                                    return@withContext TempInfo(realTemp = temp / 1000, supported = true)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            TempInfo(supported = false, error = "GPU temp not available")
        } catch (e: Exception) {
            TempInfo(supported = false, error = e.message)
        }
    }

    private suspend fun getCpuFreqInfo(): List<FreqInfo> = withContext(Dispatchers.IO) {
        val cpuFreqs = mutableListOf<FreqInfo>()
        try {
            val cpuDir = File("/sys/devices/system/cpu")
            val cpuDirs = cpuDir.listFiles()?.filter {
                it.name.startsWith("cpu") && it.name.length <= 4
            } ?: emptyList()

            for (cpu in cpuDirs) {
                try {
                    val freqDir = File(cpu, "cpufreq")
                    if (freqDir.exists()) {
                        val curFreq = File(freqDir, "scaling_cur_freq").readText().trim().toLongOrNull() ?: 0
                        val minFreq = File(freqDir, "scaling_min_freq").readText().trim().toLongOrNull() ?: 0
                        val maxFreq = File(freqDir, "scaling_max_freq").readText().trim().toLongOrNull() ?: 0

                        if (curFreq > 0) {
                            cpuFreqs.add(FreqInfo(
                                currentFreq = curFreq / 1000,
                                minFreq = minFreq / 1000,
                                maxFreq = maxFreq / 1000,
                                supported = true
                            ))
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        } catch (e: Exception) {
            Logger.e("Failed to get CPU freq", e)
        }
        return@withContext cpuFreqs
    }

    private suspend fun getGpuFreqInfo(): FreqInfo = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = shizukuManager.executeShellCommand("cat /sys/class/kgsl/kgsl-3d0/gpuclk 2>/dev/null")
            if (result.isNotEmpty() && !result.contains("Error")) {
                val freq = result.trim().toLongOrNull() ?: 0
                if (freq > 0) {
                    return@withContext FreqInfo(currentFreq = freq / 1000, supported = true)
                }
            }
            FreqInfo(supported = false, error = "GPU freq not available")
        } catch (e: Exception) {
            FreqInfo(supported = false, error = e.message)
        }
    }

    private suspend fun getCpuUsage(): Float = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = shizukuManager.executeShellCommand("top -bn1 | head -10")
            val cpuLine = result.lines().find { it.contains("CPU") }
            if (cpuLine != null) {
                val match = Regex("(\\d+\\.?\\d*)%").find(cpuLine)
                match?.value?.removeSuffix("%")?.toFloatOrNull() ?: 0f
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    suspend fun setCpuTemperature(temp: Int): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val thermalZones = File("/sys/class/thermal").listFiles()?.filter {
                it.name.startsWith("thermal_zone")
            } ?: emptyList()

            var success = false
            for (zone in thermalZones) {
                try {
                    val typeFile = File(zone, "type")
                    if (typeFile.exists()) {
                        val type = typeFile.readText().trim()
                        if (type.contains("cpu", ignoreCase = true)) {
                            val result = shizukuManager.executeShellCommand(
                                "echo ${temp * 1000} > ${zone.path}/temp"
                            )
                            if (!result.contains("Permission denied")) {
                                success = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            if (success) {
                "CPU temperature set to $temp°C"
            } else {
                "Error: CPU temperature spoofing requires root access"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun setGpuTemperature(temp: Int): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val thermalZones = File("/sys/class/thermal").listFiles()?.filter {
                it.name.startsWith("thermal_zone")
            } ?: emptyList()

            var success = false
            for (zone in thermalZones) {
                try {
                    val typeFile = File(zone, "type")
                    if (typeFile.exists()) {
                        val type = typeFile.readText().trim()
                        if (type.contains("gpu", ignoreCase = true)) {
                            val result = shizukuManager.executeShellCommand(
                                "echo ${temp * 1000} > ${zone.path}/temp"
                            )
                            if (!result.contains("Permission denied")) {
                                success = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            if (success) {
                "GPU temperature set to $temp°C"
            } else {
                "Error: GPU temperature spoofing requires root access"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun resetAllTemperatures(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val batteryResult = shizukuManager.executeShellCommand("dumpsys battery reset")
            Logger.d("Battery reset result: $batteryResult")
            
            "All temperatures reset to real values"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

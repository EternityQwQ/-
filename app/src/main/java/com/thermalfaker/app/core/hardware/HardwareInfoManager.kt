package com.thermalfaker.app.core.hardware

import com.thermalfaker.app.core.shizuku.ShizukuManager
import com.thermalfaker.app.core.util.Logger
import com.thermalfaker.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareInfoManager @Inject constructor(
    private val shizukuManager: ShizukuManager
) {
    suspend fun getAllHardwareInfo(): HardwareInfo = withContext(Dispatchers.IO) {
        try {
            HardwareInfo(
                cpuTemp = getCpuTemperature(),
                batteryTemp = getBatteryTemperature(),
                gpuTemp = getGpuTemperature(),
                ambientTemp = getAmbientTemperature(),
                cpuFrequencies = getCpuFrequencies(),
                gpuFrequency = getGpuFrequency(),
                isCpuSupported = isCpuTempSupported(),
                isGpuSupported = isGpuTempSupported(),
                isAmbientSupported = isAmbientTempSupported(),
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Logger.e("Failed to get hardware info", e)
            HardwareInfo()
        }
    }

    suspend fun getBatteryTemperature(): Int = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("dumpsys battery")
            parseBatteryTemperature(result) ?: 0
        } catch (e: Exception) {
            Logger.e("Failed to get battery temperature", e)
            0
        }
    }

    private fun parseBatteryTemperature(output: String): Int? {
        val tempLine = output.lines().find { it.contains("temperature", ignoreCase = true) }
        return tempLine?.let {
            val match = Regex("""temperature:\s*(\d+)""").find(it)
            match?.groupValues?.get(1)?.toIntOrNull()?.let { temp -> temp / 10 }
        }
    }

    suspend fun getCpuTemperature(): Int = withContext(Dispatchers.IO) {
        try {
            val zones = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/thermal/thermal_zone2/temp",
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp"
            )
            for (zone in zones) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $zone 2>/dev/null")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null && temp > 0) {
                        return@withContext if (temp > 1000) temp / 1000 else temp
                    }
                } catch (_: Exception) {
                    continue
                }
            }
            -1
        } catch (e: Exception) {
            Logger.e("Failed to get CPU temperature", e)
            -1
        }
    }

    suspend fun getGpuTemperature(): Int = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/thermal/thermal_zone5/temp",
                "/sys/class/thermal/thermal_zone6/temp",
                "/sys/class/thermal/thermal_zone7/temp"
            )
            for (path in paths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path 2>/dev/null")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null && temp > 0) {
                        return@withContext if (temp > 1000) temp / 1000 else temp
                    }
                } catch (_: Exception) {
                    continue
                }
            }
            -1
        } catch (e: Exception) {
            Logger.e("Failed to get GPU temperature", e)
            -1
        }
    }

    suspend fun getAmbientTemperature(): Int = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/thermal/thermal_zone8/temp",
                "/sys/class/thermal/thermal_zone9/temp",
                "/sys/class/thermal/thermal_zone10/temp"
            )
            for (path in paths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path 2>/dev/null")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null && temp >= 0) {
                        return@withContext if (temp > 1000) temp / 1000 else temp
                    }
                } catch (_: Exception) {
                    continue
                }
            }
            -1
        } catch (e: Exception) {
            Logger.e("Failed to get ambient temperature", e)
            -1
        }
    }

    suspend fun getCpuFrequencies(): List<CpuCoreInfo> = withContext(Dispatchers.IO) {
        try {
            val cores = mutableListOf<CpuCoreInfo>()
            for (i in 0..7) {
                try {
                    val curFreqResult = shizukuManager.executeShellCommand(
                        "cat /sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq 2>/dev/null"
                    )
                    val curFreq = curFreqResult.trim().toIntOrNull() ?: 0

                    val maxFreqResult = shizukuManager.executeShellCommand(
                        "cat /sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq 2>/dev/null"
                    )
                    val maxFreq = maxFreqResult.trim().toIntOrNull() ?: 0

                    cores.add(CpuCoreInfo(
                        coreId = i,
                        currentFrequency = if (curFreq > 1000) curFreq / 1000 else curFreq,
                        maxFrequency = if (maxFreq > 1000) maxFreq / 1000 else maxFreq
                    ))
                } catch (_: Exception) {
                    cores.add(CpuCoreInfo(coreId = i, currentFrequency = 0, maxFrequency = 0))
                }
            }
            cores
        } catch (e: Exception) {
            Logger.e("Failed to get CPU frequencies", e)
            emptyList()
        }
    }

    suspend fun getGpuFrequency(): Int = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
                "/sys/class/devfreq/gpufreq/cur_freq",
                "/sys/class/devfreq/1c30000.mali/cur_freq"
            )
            for (path in paths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path 2>/dev/null")
                    val freq = result.trim().toLongOrNull()
                    if (freq != null && freq > 0) {
                        return@withContext (freq / 1000000).toInt()
                    }
                } catch (_: Exception) {
                    continue
                }
            }
            0
        } catch (e: Exception) {
            Logger.e("Failed to get GPU frequency", e)
            0
        }
    }

    fun isCpuTempSupported(): Boolean = true
    fun isGpuTempSupported(): Boolean = true
    fun isAmbientTempSupported(): Boolean = true
}

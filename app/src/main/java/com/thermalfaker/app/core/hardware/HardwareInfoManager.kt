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
                cpuTempSupported = isCpuTempSupported(),
                batteryTemp = getBatteryTemperature(),
                batteryTempSupported = true,
                gpuTemp = getGpuTemperature(),
                gpuTempSupported = isGpuTempSupported(),
                boardTemp = getBoardTemperature(),
                boardTempSupported = isBoardTempSupported(),
                cpuFrequency = getCpuFrequencies(),
                gpuFrequency = getGpuFrequency(),
                gpuFrequencySupported = isGpuFrequencySupported(),
                thermalZones = getThermalZones()
            )
        } catch (e: Exception) {
            Logger.e("Failed to get hardware info", e)
            HardwareInfo()
        }
    }

    suspend fun getBatteryTemperature(): Int? = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("dumpsys battery")
            parseBatteryTemperature(result)
        } catch (e: Exception) {
            Logger.e("Failed to get battery temperature", e)
            null
        }
    }

    private fun parseBatteryTemperature(output: String): Int? {
        val tempLine = output.lines().find { it.contains("temperature", ignoreCase = true) }
        return tempLine?.let {
            val match = Regex("""temperature:\s*(\d+)""").find(it)
            match?.groupValues?.get(1)?.toIntOrNull()?.let { temp -> temp / 10 }
        }
    }

    suspend fun getCpuTemperature(): Int? = withContext(Dispatchers.IO) {
        try {
            val result = shizukuManager.executeShellCommand("dumpsys thermalservice")
            parseThermalServiceTemp(result) ?: readCpuTempFromSysfs()
        } catch (e: Exception) {
            Logger.e("Failed to get CPU temperature", e)
            readCpuTempFromSysfs()
        }
    }

    private fun parseThermalServiceTemp(output: String): Int? {
        val tempLine = output.lines().find { 
            it.contains("cpu", ignoreCase = true) && it.contains("temperature", ignoreCase = true) 
        }
        return tempLine?.let {
            val match = Regex("""(\d+)\s*(?:°|deg)""").find(it)
            match?.groupValues?.get(1)?.toIntOrNull()
        }
    }

    private suspend fun readCpuTempFromSysfs(): Int? {
        return try {
            val zones = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
            )
            for (zone in zones) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $zone")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null && temp > 0) {
                        return@withContext temp / 1000
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            Logger.e("Failed to read CPU temp from sysfs", e)
            null
        }
    }

    suspend fun getGpuTemperature(): Int? = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/thermal/thermal_zone2/temp",
                "/sys/devices/platform/gs101-tmu-v2/sensor-TMU-v2/temperature",
                "/sys/class/hwmon/hwmon1/temp1_input"
            )
            for (path in paths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null) {
                        return@withContext if (temp > 1000) temp / 1000 else temp
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            Logger.e("Failed to get GPU temperature", e)
            null
        }
    }

    suspend fun getBoardTemperature(): Int? = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp",
                "/sys/devices/virtual/thermal/thermal_zone1/temp"
            )
            for (path in paths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null) {
                        return@withContext if (temp > 1000) temp / 1000 else temp
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            Logger.e("Failed to get board temperature", e)
            null
        }
    }

    suspend fun getCpuFrequencies(): List<CpuCoreInfo> = withContext(Dispatchers.IO) {
        try {
            val cores = mutableListOf<CpuCoreInfo>()
            for (i in 0..7) {
                try {
                    val curFreq = readCpuFreq("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                    val minFreq = readCpuFreq("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq")
                    val maxFreq = readCpuFreq("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                    
                    if (curFreq != null) {
                        cores.add(CpuCoreInfo(
                            coreIndex = i,
                            currentFreqMHz = curFreq / 1000,
                            minFreqMHz = (minFreq ?: 0) / 1000,
                            maxFreqMHz = (maxFreq ?: 0) / 1000
                        ))
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            cores
        } catch (e: Exception) {
            Logger.e("Failed to get CPU frequencies", e)
            emptyList()
        }
    }

    private suspend fun readCpuFreq(path: String): Int? {
        return try {
            val result = shizukuManager.executeShellCommand("cat $path")
            result.trim().toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGpuFrequency(): Int? = withContext(Dispatchers.IO) {
        try {
            val paths = listOf(
                "/sys/class/devfreq/1c30000.mali/cur_freq",
                "/sys/class/devfreq/gs101-devfreq-mali/cur_freq"
            )
            for (path in paths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path")
                    val freq = result.trim().toLongOrNull()
                    if (freq != null) {
                        return@withContext (freq / 1000000).toInt()
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            Logger.e("Failed to get GPU frequency", e)
            null
        }
    }

    suspend fun getThermalZones(): List<ThermalZoneInfo> = withContext(Dispatchers.IO) {
        try {
            val zones = mutableListOf<ThermalZoneInfo>()
            val result = shizukuManager.executeShellCommand(
                "for zone in /sys/class/thermal/thermal_zone*; do echo \"\$(basename \$zone):\$(cat \$zone/temp 2>/dev/null):\$(cat \$zone/type 2>/dev/null)\"; done"
            )
            result.lines().forEach { line ->
                val parts = line.split(":")
                if (parts.size >= 3) {
                    val temp = parts[1].toIntOrNull()
                    if (temp != null) {
                        zones.add(ThermalZoneInfo(
                            name = parts[0],
                            path = "/sys/class/thermal/${parts[0]}",
                            temperature = if (temp > 1000) temp / 1000 else temp,
                            type = parts[2]
                        ))
                    }
                }
            }
            zones.take(10)
        } catch (e: Exception) {
            Logger.e("Failed to get thermal zones", e)
            emptyList()
        }
    }

    fun isCpuTempSupported(): Boolean = true
    fun isGpuTempSupported(): Boolean = true
    fun isBoardTempSupported(): Boolean = true
    fun isGpuFrequencySupported(): Boolean = true
}
package com.thermalfakerpackage com.thermalfaker.app.core.hardware

import android.content.Context
import com.thermalfaker.app.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
importpackage com.thermalfaker.app.core.hardware

import android.content.Context
import com.thermalfaker.app.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
package com.thermalfaker.app.core.hardware

import android.content.Context
import com.thermalfaker.app.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
classpackage com.thermalfaker.app.core.hardware

import android.content.Context
import com.thermalfaker.app.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareInfoManager @Inject constructor(
    private val context: Context
) {

    suspendpackage com.thermalfaker.app.core.hardware

import android.content.Context
import com.thermalfaker.app.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareInfoManager @Inject constructor(
    private val context: Context
) {

    suspend fun readCpuTemperature(): Float = withContext
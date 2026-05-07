package com.thermalfaker.app.core.shizuku

import com.thermalfaker.app.data.model.CpuCoreInfo
import com.thermalfaker.app.data.model.HardwareInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareMonitor @Inject constructor(
    private val shizukuManager: ShizukuManager
) {
    private val _hardwareInfo = MutableStateFlow(HardwareInfo())
    val hardwareInfo: StateFlow<HardwareInfo> = _hardwareInfo.asStateFlow()

    suspend fun refreshHardwareInfo(): Result<HardwareInfo> = withContext(Dispatchers.IO) {
        try {
            val batteryTemp = getBatteryTemperature()
            val cpuTemp = getCpuTemperature()
            val gpuTemp = getGpuTemperature()
            val ambientTemp = getAmbientTemperature()
            val cpuFrequencies = getCpuFrequencies()
            val gpuFrequency = getGpuFrequency()

            val info = HardwareInfo(
                batteryTemp = batteryTemp,
                cpuTemp = cpuTemp,
                gpuTemp = gpuTemp,
                ambientTemp = ambientTemp,
                cpuFrequencies = cpuFrequencies,
                gpuFrequency = gpuFrequency,
                isCpuSupported = cpuTemp >= 0,
                isGpuSupported = gpuTemp >= 0,
                isAmbientSupported = ambientTemp >= 0,
                lastUpdated = System.currentTimeMillis()
            )

            _hardwareInfo.value = info
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getBatteryTemperature(): Int {
        return try {
            val result = shizukuManager.executeShellCommand("dumpsys battery | grep temperature")
            val regex = "temperature: (\\d+)".toRegex()
            val match = regex.find(result)
            match?.groupValues?.get(1)?.toInt()?.div(10) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun getCpuTemperature(): Int {
        return try {
            val thermalZones = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/thermal/thermal_zone2/temp",
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp",
                "/sys/class/thermal/thermal_zone5/temp",
                "/sys/class/thermal/thermal_zone6/temp",
                "/sys/class/thermal/thermal_zone7/temp",
                "/sys/class/thermal/thermal_zone8/temp",
                "/sys/class/thermal/thermal_zone9/temp"
            )

            var cpuTemp = -1

            for (zone in thermalZones) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $zone 2>/dev/null")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null && temp > 0) {
                        val normalizedTemp = if (temp > 1000) temp / 1000 else temp
                        if (normalizedTemp in 10..120) {
                            cpuTemp = normalizedTemp
                            break
                        }
                    }
                } catch (_: Exception) {
                    continue
                }
            }

            if (cpuTemp == -1) {
                try {
                    val result = shizukuManager.executeShellCommand("dumpsys thermalservice | grep -i cpu")
                    val regex = "([0-9]+\\.?[0-9]*)".toRegex()
                    val match = regex.find(result)
                    cpuTemp = match?.groupValues?.get(1)?.toFloatOrNull()?.toInt() ?: -1
                } catch (_: Exception) {
                }
            }

            cpuTemp
        } catch (e: Exception) {
            -1
        }
    }

    private fun getGpuTemperature(): Int {
        return try {
            val gpuThermalZones = listOf(
                "/sys/class/thermal/thermal_zone10/temp",
                "/sys/class/thermal/thermal_zone11/temp",
                "/sys/class/thermal/thermal_zone12/temp",
                "/sys/class/thermal/thermal_zone13/temp",
                "/sys/class/thermal/thermal_zone14/temp",
                "/sys/class/thermal/thermal_zone15/temp",
                "/sys/class/thermal/thermal_zone16/temp",
                "/sys/class/thermal/thermal_zone17/temp",
                "/sys/class/thermal/thermal_zone18/temp",
                "/sys/class/thermal/thermal_zone19/temp"
            )

            var gpuTemp = -1

            for (zone in gpuThermalZones) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $zone 2>/dev/null")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null && temp > 0) {
                        val normalizedTemp = if (temp > 1000) temp / 1000 else temp
                        if (normalizedTemp in 10..120) {
                            gpuTemp = normalizedTemp
                            break
                        }
                    }
                } catch (_: Exception) {
                    continue
                }
            }

            if (gpuTemp == -1) {
                try {
                    val result = shizukuManager.executeShellCommand("dumpsys thermalservice | grep -i gpu")
                    val regex = "([0-9]+\\.?[0-9]*)".toRegex()
                    val match = regex.find(result)
                    gpuTemp = match?.groupValues?.get(1)?.toFloatOrNull()?.toInt() ?: -1
                } catch (_: Exception) {
                }
            }

            gpuTemp
        } catch (e: Exception) {
            -1
        }
    }

    private fun getAmbientTemperature(): Int {
        return try {
            val ambientPaths = listOf(
                "/sys/class/thermal/thermal_zone20/temp",
                "/sys/class/thermal/thermal_zone21/temp",
                "/sys/class/thermal/thermal_zone22/temp",
                "/sys/class/thermal/thermal_zone23/temp",
                "/sys/class/thermal/thermal_zone24/temp",
                "/sys/class/thermal/thermal_zone25/temp",
                "/sys/class/thermal/thermal_zone26/temp",
                "/sys/class/thermal/thermal_zone27/temp",
                "/sys/class/thermal/thermal_zone28/temp",
                "/sys/class/thermal/thermal_zone29/temp"
            )

            var ambientTemp = -1

            for (path in ambientPaths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path 2>/dev/null")
                    val temp = result.trim().toIntOrNull()
                    if (temp != null && temp > 0) {
                        val normalizedTemp = if (temp > 1000) temp / 1000 else temp
                        if (normalizedTemp in 0..60) {
                            ambientTemp = normalizedTemp
                            break
                        }
                    }
                } catch (_: Exception) {
                    continue
                }
            }

            ambientTemp
        } catch (e: Exception) {
            -1
        }
    }

    private fun getCpuFrequencies(): List<CpuCoreInfo> {
        val cores = mutableListOf<CpuCoreInfo>()

        try {
            val cpuCountResult = shizukuManager.executeShellCommand("ls /sys/devices/system/cpu/ | grep -c 'cpu[0-9]'")
            val cpuCount = cpuCountResult.trim().toIntOrNull() ?: 8

            for (i in 0 until cpuCount.coerceAtMost(16)) {
                try {
                    val freqResult = shizukuManager.executeShellCommand(
                        "cat /sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq 2>/dev/null"
                    )
                    val freq = freqResult.trim().toIntOrNull() ?: 0
                    val freqMHz = if (freq > 1000) freq / 1000 else freq

                    val maxFreqResult = shizukuManager.executeShellCommand(
                        "cat /sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq 2>/dev/null"
                    )
                    val maxFreq = maxFreqResult.trim().toIntOrNull() ?: 0
                    val maxFreqMHz = if (maxFreq > 1000) maxFreq / 1000 else maxFreq

                    cores.add(CpuCoreInfo(
                        coreId = i,
                        currentFrequency = freqMHz,
                        maxFrequency = maxFreqMHz
                    ))
                } catch (_: Exception) {
                    cores.add(CpuCoreInfo(
                        coreId = i,
                        currentFrequency = 0,
                        maxFrequency = 0
                    ))
                }
            }
        } catch (e: Exception) {
        }

        return cores
    }

    private fun getGpuFrequency(): Int {
        return try {
            val gpuPaths = listOf(
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
                "/sys/class/misc/mali0/device/clock",
                "/sys/class/misc/mali0/device/clk",
                "/sys/kernel/gpu/gpu_clock",
                "/sys/class/devfreq/gpufreq/cur_freq",
                "/sys/class/devfreq/fb000000.gpu/cur_freq",
                "/sys/class/devfreq/fde60000.gpu/cur_freq"
            )

            var gpuFreq = 0

            for (path in gpuPaths) {
                try {
                    val result = shizukuManager.executeShellCommand("cat $path 2>/dev/null")
                    val freq = result.trim().toIntOrNull()
                    if (freq != null && freq > 0) {
                        gpuFreq = if (freq > 1000000) freq / 1000000 else if (freq > 1000) freq / 1000 else freq
                        break
                    }
                } catch (_: Exception) {
                    continue
                }
            }

            gpuFreq
        } catch (e: Exception) {
            0
        }
    }
}

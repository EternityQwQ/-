package com.thermalfaker.app.core.shizuku

import android.content.Context
import android.os.BatteryManager
import com.thermalfaker.app.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizukuManager: ShizukuManager
) {

    suspend fun setBatteryTemperature(temperatureCelsius: Int): String = withContext(Dispatchers.IO) {
        try {
            val temperatureTenths = temperatureCelsius * 10
            val result = shizukuManager.executeShellCommand("dumpsys battery set temp $temperatureTenths")
            Logger.d("Set battery temp result: $result")
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
            result
        } catch (e: Exception) {
            Logger.e("Failed to reset battery temperature", e)
            "Error: ${e.message}"
        }
    }

    fun getCurrentBatteryTemperature(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            batteryManager?.run {
                javaClass.getMethod("getIntProperty", Int::class.java)
                    .invoke(this, 7) as? Int
            }?.let { it / 10 } ?: 0
        } catch (e: Exception) {
            Logger.e("Failed to get battery temperature", e)
            0
        }
    }
}

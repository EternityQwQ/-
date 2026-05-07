package com.thermalfaker.app.core.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import com.thermalfaker.app.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.rikka.shizuku.Shizuku
import dev.rikka.shizuku.ShizukuProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class ShizukuStatus {
    data object Unavailable : ShizukuStatus()
    data object NotInstalled : ShizukuStatus()
    data object PermissionDenied : ShizukuStatus()
    data object PermissionGranted : ShizukuStatus()
    data object Connected : ShizukuStatus()
}

@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _status = MutableStateFlow<ShizukuStatus>(ShizukuStatus.Unavailable)
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val permissionRequestCode = 1001

    init {
        checkShizuku()
    }

    private fun checkShizuku() {
        try {
            if (!Shizuku.isPreV11()) {
                _status.value = ShizukuStatus.Unavailable
                return
            }
        } catch (e: Exception) {
            Logger.e("Shizuku is not available", e)
            _status.value = ShizukuStatus.NotInstalled
            return
        }

        if (checkSelfPermission()) {
            _status.value = ShizukuStatus.PermissionGranted
        } else {
            _status.value = ShizukuStatus.PermissionDenied
        }
    }

    fun checkSelfPermission(): Boolean {
        return try {
            if (!Shizuku.isPreV11()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Logger.e("Failed to check Shizuku permission", e)
            false
        }
    }

    fun requestPermission() {
        try {
            if (!Shizuku.isPreV11()) {
                _status.value = ShizukuStatus.Unavailable
                return
            }
            if (Shizuku.shouldShowRequestPermissionRationale()) {
                _status.value = ShizukuStatus.PermissionDenied
                return
            }
            Shizuku.requestPermission(permissionRequestCode)
        } catch (e: Exception) {
            Logger.e("Failed to request Shizuku permission", e)
        }
    }

    fun executeCommand(command: String): String {
        return try {
            if (!checkSelfPermission()) {
                return "Error: Shizuku permission not granted"
            }
            val process = Runtime.getRuntime().exec("sh")
            process.outputStream.bufferedWriter().use { it.write(command) }
            process.waitFor()
            val result = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            if (error.isNotEmpty()) Logger.e("Command error: $error")
            result
        } catch (e: Exception) {
            Logger.e("Failed to execute command", e)
            "Error: ${e.message}"
        }
    }

    fun executeShellCommand(command: String): String {
        return try {
            if (!checkSelfPermission()) {
                return "Error: Shizuku permission not granted"
            }
            val result = Shizuku.exec(arrayOf("sh", "-c", command), null, null)
            return buildString {
                if (result.out != null) append(String(result.out))
                if (result.err != null) append("\nError: ").append(String(result.err))
            }
        } catch (e: RemoteException) {
            Logger.e("Failed to execute shell command via Shizuku", e)
            "Error: ${e.message}"
        }
    }
}

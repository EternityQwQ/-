package com.thermalfaker.app.core.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.os.RemoteException
import com.thermalfaker.app.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

sealed class ShizukuStatus {
    data object Unavailable : ShizukuStatus()
    data object NotInstalled : ShizukuStatus()
    data object PermissionDenied : ShizukuStatus()
    data object PermissionGranted : ShizukuStatus()
    data object BinderReceived : ShizukuStatus()
}

@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _status = MutableStateFlow<ShizukuStatus>(ShizukuStatus.Unavailable)
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val permissionRequestCode = 1001

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Logger.d("Shizuku binder received")
        _status.value = ShizukuStatus.BinderReceived
        checkPermissionStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Logger.d("Shizuku binder dead")
        _status.value = ShizukuStatus.Unavailable
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == permissionRequestCode) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Logger.d("Shizuku permission granted")
                _status.value = ShizukuStatus.PermissionGranted
            } else {
                Logger.d("Shizuku permission denied")
                _status.value = ShizukuStatus.PermissionDenied
            }
        }
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Logger.d("Shizuku listeners registered")
        } catch (e: Throwable) {
            Logger.e("Failed to register Shizuku listeners", e)
            _status.value = ShizukuStatus.NotInstalled
        }
    }

    private fun checkPermissionStatus() {
        try {
            when {
                Shizuku.isPreV11() -> {
                    Logger.d("Shizuku pre-v11 detected")
                    _status.value = ShizukuStatus.Unavailable
                }
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                    Logger.d("Shizuku permission already granted")
                    _status.value = ShizukuStatus.PermissionGranted
                }
                Shizuku.shouldShowRequestPermissionRationale() -> {
                    Logger.d("Shizuku permission rationale shown")
                    _status.value = ShizukuStatus.PermissionDenied
                }
                else -> {
                    _status.value = ShizukuStatus.PermissionDenied
                }
            }
        } catch (e: Throwable) {
            Logger.e("Error checking Shizuku permission", e)
            _status.value = ShizukuStatus.NotInstalled
        }
    }

    fun checkSelfPermission(): Boolean {
        return try {
            !Shizuku.isPreV11() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            Logger.e("Failed to check Shizuku permission", e)
            false
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.isPreV11()) {
                _status.value = ShizukuStatus.Unavailable
                return
            }
            Shizuku.requestPermission(permissionRequestCode)
        } catch (e: Throwable) {
            Logger.e("Failed to request Shizuku permission", e)
            _status.value = ShizukuStatus.NotInstalled
        }
    }

    suspend fun executeShellCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            if (!checkSelfPermission()) {
                return@withContext "Error: Shizuku permission not granted"
            }
            val uid = Shizuku.getUid()
            Logger.d("Executing as UID: $uid")
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = reader.readText()
            val error = errorReader.readText()
            process.waitFor()
            reader.close()
            errorReader.close()
            buildString {
                if (output.isNotEmpty()) append(output)
                if (error.isNotEmpty()) append("\nError: ").append(error)
            }
        } catch (e: RemoteException) {
            Logger.e("Failed to execute shell command via Shizuku", e)
            "Error: ${e.message}"
        } catch (e: Throwable) {
            Logger.e("Unexpected error executing shell command", e)
            "Error: ${e.message}"
        }
    }

    fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Throwable) {
            Logger.e("Error cleaning up Shizuku listeners", e)
        }
    }
}

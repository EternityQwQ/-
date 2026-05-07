package com.thermalfaker.app.core.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.thermalfaker.app.BuildConfig
import com.thermalfaker.app.IShellService
import com.thermalfaker.app.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume
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
    @ApplicationContext private val context: android.content.Context
) {
    private val _status = MutableStateFlow<ShizukuStatus>(ShizukuStatus.Unavailable)
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val permissionRequestCode = 1001

    @Volatile
    private var shellService: IShellService? = null

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, "com.thermalfaker.app.core.shizuku.ShellService")
    ).processNameSuffix("shell_service")
        .version(BuildConfig.VERSION_CODE)
        .debuggable(BuildConfig.DEBUG)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            Logger.d("ShellService connected")
            shellService = IShellService.Stub.asInterface(binder)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Logger.d("ShellService disconnected")
            shellService = null
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Logger.d("Shizuku binder received")
        _status.value = ShizukuStatus.BinderReceived
        checkPermissionStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Logger.d("Shizuku binder dead")
        _status.value = ShizukuStatus.Unavailable
        shellService = null
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
                    _status.value = ShizukuStatus.Unavailable
                }
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                    _status.value = ShizukuStatus.PermissionGranted
                }
                Shizuku.shouldShowRequestPermissionRationale() -> {
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

    private suspend fun ensureServiceBound() {
        shellService?.let {
            if (it.asBinder().pingBinder()) return
        }

        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val conn = object : ServiceConnection {
                    override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                        Logger.d("ShellService connected (ensureServiceBound)")
                        shellService = IShellService.Stub.asInterface(binder)
                        continuation.resume(Unit)
                    }

                    override fun onServiceDisconnected(componentName: ComponentName) {
                        Logger.d("ShellService disconnected (ensureServiceBound)")
                        shellService = null
                    }
                }
                Shizuku.bindUserService(userServiceArgs, conn)
                continuation.invokeOnCancellation {
                    try {
                        Shizuku.unbindUserService(userServiceArgs, conn, true)
                    } catch (_: Throwable) {}
                }
            }
        }
    }

    suspend fun executeShellCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            if (!checkSelfPermission()) {
                return@withContext "Error: Shizuku permission not granted"
            }

            ensureServiceBound()

            val service = shellService
                ?: return@withContext "Error: Shell service not connected"

            service.executeCommand(command)
        } catch (e: Throwable) {
            Logger.e("Failed to execute shell command", e)
            "Error: ${e.message}"
        }
    }

    fun unbindService() {
        try {
            shellService?.let {
                if (it.asBinder().pingBinder()) {
                    Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
                }
            }
            shellService = null
        } catch (e: Throwable) {
            Logger.e("Error unbinding ShellService", e)
        }
    }

    fun cleanup() {
        try {
            unbindService()
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Throwable) {
            Logger.e("Error cleaning up", e)
        }
    }
}

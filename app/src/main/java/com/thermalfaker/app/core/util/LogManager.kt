package com.thermalfaker.app.core.util

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String
)

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

@Singleton
class LogManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val maxLogs = 1000

    fun d(tag: String, message: String) {
        addLog(LogLevel.DEBUG, tag, message)
        android.util.Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        addLog(LogLevel.INFO, tag, message)
        android.util.Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        addLog(LogLevel.WARN, tag, message)
        android.util.Log.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        addLog(LogLevel.ERROR, tag, fullMessage)
        android.util.Log.e(tag, message, throwable)
    }

    private fun addLog(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message
        )

        _logs.value = listOf(entry) + _logs.value.take(maxLogs - 1)
    }

    suspend fun exportLogs(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "ThermalFaker_log_$timestamp.txt"
            val file = File(downloadDir, fileName)

            FileWriter(file).use { writer ->
                for (log in _logs.value.reversed()) {
                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
                    writer.write("[${log.level.name}] $timeStr ${log.tag}: ${log.message}\n")
                }
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun getLogCount(): Int = _logs.value.size
}

class LogViewModel @Inject constructor(
    private val logManager: LogManager
) : ViewModel() {

    val logs = logManager.logs

    fun exportLogs() = logManager.exportLogs()

    fun clearLogs() = logManager.clearLogs()

    fun getLogCount() = logManager.getLogCount()
}

package com.thermalfaker.app.core.util

object Logger {
    private const val TAG = "ThermalFaker"

    private var logManager: LogManager? = null

    fun initialize(manager: LogManager) {
        logManager = manager
    }

    fun d(message: String) {
        logManager?.d(TAG, message) ?: android.util.Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        logManager?.e(TAG, message, throwable) ?: android.util.Log.e(TAG, message, throwable)
    }

    fun w(message: String) {
        logManager?.w(TAG, message) ?: android.util.Log.w(TAG, message)
    }

    fun i(tag: String, message: String) {
        logManager?.i(tag, message) ?: android.util.Log.i(tag, message)
    }

    fun i(message: String) {
        logManager?.i(TAG, message) ?: android.util.Log.i(TAG, message)
    }
}

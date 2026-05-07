package com.thermalfaker.app.core.util

import android.util.Log

object Logger {
    private const val TAG = "ThermalFaker"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }
}

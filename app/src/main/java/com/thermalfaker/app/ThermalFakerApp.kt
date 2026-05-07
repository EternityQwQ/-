package com.thermalfaker.app

import android.app.Application
import com.thermalfaker.app.core.util.Logger
import com.thermalfaker.app.core.util.LogManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ThermalFakerApp : Application() {

    @Inject
    lateinit var logManager: LogManager

    override fun onCreate() {
        super.onCreate()
        Logger.initialize(logManager)
    }
}

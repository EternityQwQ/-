package com.thermalfaker.app.core.shizuku

import android.content.Context
import androidx.annotation.Keep
import com.thermalfaker.app.IShellService
import java.io.BufferedReader
import java.io.InputStreamReader

class ShellService : IShellService.Stub {

    constructor()

    @Keep
    constructor(_context: Context) {}

    override fun destroy() {
        System.exit(0)
    }

    override fun exit() {
        destroy()
    }

    override fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = outputReader.readText()
            val error = errorReader.readText()
            process.waitFor()
            outputReader.close()
            errorReader.close()
            buildString {
                if (output.isNotEmpty()) append(output)
                if (error.isNotEmpty()) append("\nError: ").append(error)
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

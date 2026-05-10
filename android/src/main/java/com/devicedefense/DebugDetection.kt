package com.devicedefense

import android.content.Context
import android.os.Debug
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Detection for debuggers and debugging tools
 */
class DebugDetection(private val context: Context) {

    /**
     * Check if app is debuggable
     */
    fun isDebuggable(): Boolean {
        // Check application flags
        val appInfo = context.applicationInfo
        if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            return true
        }

        // Check if debugger is connected
        if (Debug.isDebuggerConnected()) {
            return true
        }

        // Check if debugger is waiting
        if (Debug.waitingForDebugger()) {
            return true
        }

        // Check for tracer PID
        if (checkTracerPid()) {
            return true
        }

        return false
    }

    /**
     * Check for tracer PID in /proc/self/status
     * Tracer PID > 0 indicates a debugger is attached
     */
    private fun checkTracerPid(): Boolean {
        try {
            val statusFile = File("/proc/self/status")
            if (!statusFile.exists()) {
                return false
            }

            val bufferedReader = BufferedReader(InputStreamReader(statusFile.inputStream()))
            bufferedReader.use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line?.startsWith("TracerPid:") == true) {
                        val tracerPid = line.substringAfter(":").trim()
                        val pid = tracerPid.toIntOrNull()
                        // TracerPid > 0 means a debugger is attached
                        return pid != null && pid > 0
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        return false
    }

    /**
     * Check for debugging tools installed
     */
    fun hasDebuggingTools(): Boolean {
        val debugPackages = listOf(
            "com.android.spare_parts", // Android spare parts
            "com.android.development", // Development settings
            "com.android.customlocale2", // Custom locale
            "com.android.emulator.gps", // Emulator GPS
            "com.saurik.substrate", // Substrate (cydia alternative)
            "com.n0n3m4.gpsroot", // GPS root
            "com.zachspong.temprootremovejb", // Temp root remove
            "com.ramdroid.appquarantine", // App quarantine
            "com.devadvance.rootcloak", // Root cloak
            "com.devadvance.rootcloakplus", // Root cloak plus
            "de.robv.android.xposed.installer", // Xposed
            "com.saurik.substrate", // Substrate
            "com.pyler.panel", // Xposed module
            "com.elpuerco.touches", // Touches indicator
            "com.manic.networkhandup", // Network handup
            "com.automate1234 automate", // Automate
            "com.faendir.leo.louise", // Louise
            "com.faendir.leo.louise.free", // Louise free
            "com.faendir.leo.louise.paid", // Louise paid
            "com.faendir.leo.louise.unlock", // Louise unlock
            "com.faendir.leo.louise.unlock.all" // Louise unlock all
        )

        return debugPackages.any { isPackageInstalled(it) }
    }

    /**
     * Check if ADB debugging is enabled
     */
    fun isAdbEnabled(): Boolean {
        // Check global settings for ADB
        return try {
            val settings = android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.ADB_ENABLED,
                0
            )
            settings == 1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if package is installed
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}

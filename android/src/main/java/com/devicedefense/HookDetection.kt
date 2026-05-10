package com.devicedefense

import android.content.Context
import android.content.pm.PackageManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Detection for hooking frameworks (Frida, Xposed, Magisk, etc.)
 */
class HookDetection(private val context: Context) {

    /**
     * Check if Frida framework is present
     */
    fun hasFrida(): Boolean {
        return checkFridaPorts() ||
                checkFridaLibraries() ||
                checkFridaProcesses() ||
                checkFridaFiles()
    }

    /**
     * Check if Xposed framework is present
     */
    fun hasXposed(): Boolean {
        return checkXposedApp() ||
                checkXposedInClassLoader() ||
                checkXposedFiles()
    }

    /**
     * Check if Magisk is present
     */
    fun hasMagisk(): Boolean {
        return checkMagiskApp() ||
                checkMagiskFiles() ||
                checkMagiskModules()
    }

    /**
     * Check for Frida ports (default: 27042, 27043)
     */
    private fun checkFridaPorts(): Boolean {
        val fridaPorts = listOf(27042, 27043, 27044, 27045)

        // Read /proc/net/tcp to check for open ports
        try {
            val bufferedReader = BufferedReader(InputStreamReader(
                File("/proc/net/tcp").inputStream()
            ))

            bufferedReader.use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Format: sl local_address rem_address st ...
                    // Skip header
                    if (line?.startsWith("  sl") == true) continue

                    val parts = line?.trim()?.split("\\s+".toRegex()) ?: continue
                    if (parts.size >= 2) {
                        try {
                            // local_address format: hex_ip:hex_port
                            val localAddress = parts[1]
                            val addressParts = localAddress.split(":")
                            if (addressParts.size == 2) {
                                val portHex = addressParts[1]
                                val port = portHex.toInt(16)
                                if (port in fridaPorts) {
                                    return true
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore parsing errors
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        return false
    }

    /**
     * Check for Frida libraries in memory
     */
    private fun checkFridaLibraries(): Boolean {
        // Check /proc/self/maps for Frida libraries
        try {
            val bufferedReader = BufferedReader(InputStreamReader(
                File("/proc/self/maps").inputStream()
            ))

            bufferedReader.use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line?.contains("frida") == true ||
                        line?.contains("frida-agent") == true ||
                        line?.contains("libfrida") == true) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        return false
    }

    /**
     * Check for Frida processes
     */
    private fun checkFridaProcesses(): Boolean {
        try {
            val process = Runtime.getRuntime().exec("ps")
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            reader.use {
                var line: String?
                while (it.readLine().also { line = it } != null) {
                    if (line?.contains("frida") == true ||
                        line?.contains("frida-server") == true) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        return false
    }

    /**
     * Check for Frida-related files
     */
    private fun checkFridaFiles(): Boolean {
        val fridaPaths = listOf(
            "/data/local/tmp/frida",
            "/data/local/tmp/frida-server",
            "/data/local/tmp/frida-agent.so",
            "/data/local/tmp/frida-agent-*.so",
            "/system/lib/libfrida.so",
            "/system/lib64/libfrida.so"
        )

        return fridaPaths.any { path ->
            try {
                // Handle wildcards
                if (path.contains("*")) {
                    val dir = File(path.substringBeforeLast("/"))
                    val prefix = path.substringAfterLast("/").replace("*", "")
                    dir.list()?.any { it.startsWith(prefix) } == true
                } else {
                    File(path).exists()
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Check if Xposed installer app is present
     */
    private fun checkXposedApp(): Boolean {
        val xposedPackages = listOf(
            "de.robv.android.xposed.installer",
            "de.robv.android.xposed.installer",
            "org.meowcat.edxposed.manager",
            "io.github.huskydg.xposed",
            "com.solohsu.android.edxp.manager"
        )

        return xposedPackages.any { isPackageInstalled(it) }
    }

    /**
     * Check for Xposed in class loader
     */
    private fun checkXposedInClassLoader(): Boolean {
        return try {
            // Check for XposedBridge class
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Check for Xposed-related files
     */
    private fun checkXposedFiles(): Boolean {
        val xposedPaths = listOf(
            "/system/framework/XposedBridge.jar",
            "/system/bin/app_process32_xposed",
            "/system/bin/app_process64_xposed",
            "/system/xposed.prop",
            "/cache/XposedBridge.jar"
        )

        return xposedPaths.any { File(it).exists() }
    }

    /**
     * Check if Magisk app is present
     */
    private fun checkMagiskApp(): Boolean {
        val magiskPackages = listOf(
            "com.topjohnwu.magisk",
            "com.topjohnwu.magisk.ui"
        )

        return magiskPackages.any { isPackageInstalled(it) }
    }

    /**
     * Check for Magisk-related files
     */
    private fun checkMagiskFiles(): Boolean {
        val magiskPaths = listOf(
            "/sbin/.magisk",
            "/sbin/.core/mirror",
            "/sbin/.core/img",
            "/sbin/.core/db-0/magisk.db",
            "/cache/magisk.log",
            "/dev/com.topjohnwu.magisk",
            "/data/adb/magisk",
            "/data/adb/magisk.img",
            "/data/adb/magisk.db",
            "/data/adb/magisk_simple",
            "/system/addon.d/99-magisk.sh"
        )

        return magiskPaths.any { File(it).exists() }
    }

    /**
     * Check for Magisk modules
     */
    private fun checkMagiskModules(): Boolean {
        val magiskModuleDir = "/data/adb/modules"
        val moduleDir = File(magiskModuleDir)

        if (moduleDir.exists() && moduleDir.isDirectory) {
            // Check if any modules are enabled
            val modules = moduleDir.listFiles()
            return modules?.isNotEmpty() == true
        }

        return false
    }

    /**
     * Check if package is installed
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

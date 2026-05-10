package com.devicedefense

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.scottyab.rootbeer.RootBeer
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Multi-layer root detection using various techniques
 */
class RootDetection(private val context: Context) {

    data class RootDetectionResult(
        val isRooted: Boolean,
        val hasRootBeerDetected: Boolean,
        val hasNativeRootDetected: Boolean,
        val hasDangerousBins: Boolean,
        val hasRootApps: Boolean,
        val hasSystemPropsModified: Boolean,
        val details: Map<String, Boolean>
    )

    /**
     * Perform comprehensive root detection
     */
    fun performDetection(): RootDetectionResult {
        val rootBeer = RootBeer(context)
        val hasRootBeerDetected = rootBeer.isRooted

        // Native detection (JNI)
        val hasNativeRootDetected = NativeSecurityCheck.isRooted()

        // Check for dangerous binaries
        val hasDangerousBins = checkDangerousBinaries()

        // Check for root management apps
        val hasRootApps = checkRootApps()

        // Check system properties
        val hasSystemPropsModified = checkSystemProperties()

        val details = mapOf(
            "root_beer" to hasRootBeerDetected,
            "native_detection" to hasNativeRootDetected,
            "dangerous_bins" to hasDangerousBins,
            "root_apps" to hasRootApps,
            "system_props" to hasSystemPropsModified,
            "su_binary" to checkForSuBinary(),
            "busybox_binary" to checkForBusybox(),
            "rw_system" to checkSystemRemounted(),
            "dangerous_props" to checkDangerousSystemProperties()
        )

        val isRooted = hasRootBeerDetected ||
                hasNativeRootDetected ||
                hasDangerousBins ||
                hasRootApps ||
                hasSystemPropsModified

        return RootDetectionResult(
            isRooted = isRooted,
            hasRootBeerDetected = hasRootBeerDetected,
            hasNativeRootDetected = hasNativeRootDetected,
            hasDangerousBins = hasDangerousBins,
            hasRootApps = hasRootApps,
            hasSystemPropsModified = hasSystemPropsModified,
            details = details
        )
    }

    /**
     * Check for dangerous binaries commonly found on rooted devices
     */
    private fun checkDangerousBinaries(): Boolean {
        val dangerousBins = listOf(
            "su",
            "busybox",
            "magisk",
            "supolicy",
            "supol",
            "daemonsu",
            "ksu", // KernelSU
            "apd"  // Apex (another root solution)
        )

        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/magisk/.core/bin/su",
            "/system/usr/we-need-root/su",
            "/system/app/SuperSU",
            "/system/app/SuperSU.apk",
            "/system/app/Superuser",
            "/system/app/Superuser.apk",
            "/data/data/com.noshufou.android.su",
            "/data/data/com.thirdparty.superuser",
            "/data/data/eu.chainfire.supersu",
            "/system/xbin/busybox",
            "/system/bin/busybox",
            "/data/data/com.topjohnwu.magisk",
            "/cache/magisk.log",
            "/dev/com.topjohnwu.magisk",
            "/system/app/MagiskManager"
        )

        // Check for dangerous binaries in PATH
        val pathEnv = System.getenv("PATH") ?: ""
        val pathDirs = pathEnv.split(":")

        for (bin in dangerousBins) {
            // Check in common locations
            for (path in paths) {
                if (File(path).exists()) {
                    return true
                }
            }

            // Check in PATH directories
            for (dir in pathDirs) {
                if (File(dir, bin).exists()) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Check for su binary specifically
     */
    private fun checkForSuBinary(): Boolean {
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/magisk/.core/bin/su"
        )

        return suPaths.any { File(it).exists() && File(it).canExecute() }
    }

    /**
     * Check for busybox binary
     */
    private fun checkForBusybox(): Boolean {
        val busyboxPaths = listOf(
            "/system/xbin/busybox",
            "/system/bin/busybox",
            "/sbin/busybox",
            "/data/local/xbin/busybox",
            "/data/local/bin/busybox"
        )

        return busyboxPaths.any { File(it).exists() }
    }

    /**
     * Check for root management apps
     */
    private fun checkRootApps(): Boolean {
        val rootApps = listOf(
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.topjohnwu.magisk",
            "com.topjohnwu.magisk.ui",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root",
            "com.alephzain.framaroot",
            "com.ramdroid.appquarantine",
            "com.ramdroid.appquarantinepro",
            "com.devadvance.rootcloak",
            "com.devadvance.rootcloakplus",
            "de.robv.android.xposed.installer",
            "com.saurik.substrate",
            "com.zachspong.temprootremovejb",
            "com.amphoras.hidemyroot",
            "com.amphoras.hidemyrootadfree",
            "com.formyhm.hiderootPremium",
            "com.formyhm.hideroot",
            "me.phh.superuser",
            "eu.chainfire.firefdsuid",
            "com.koushikdutta.rommanager",
            "com.koushikdutta.rommanager.license",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.luckypatcher",
            "com.android.vending.billing.InAppBillingService.COIN",
            "com.android.vending.billing.InAppBillingService.LUCK",
            "com.topjohnwu.magisk.ua"
        )

        return rootApps.any { isPackageInstalled(it) }
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

    /**
     * Check system properties for root indicators
     */
    private fun checkSystemProperties(): Boolean {
        return checkDangerousSystemProperties() ||
                checkSystemRemounted() ||
                checkForDangerousBuildProps()
    }

    /**
     * Check for dangerous system properties
     */
    private fun checkDangerousSystemProperties(): Boolean {
        val dangerousProps = listOf(
            "ro.debuggable",
            "ro.secure",
            "service.adb.root",
            "ro.build.selinux"
        )

        for (prop in dangerousProps) {
            val value = getSystemProperty(prop)
            when (prop) {
                "ro.debuggable" -> {
                    // 1 means debuggable (should be 0 on production)
                    if (value == "1") return true
                }
                "ro.secure" -> {
                    // 0 means insecure (should be 1 on production)
                    if (value == "0") return true
                }
                "service.adb.root" -> {
                    // 1 means adb root is enabled
                    if (value == "1") return true
                }
            }
        }

        return false
    }

    /**
     * Check if system is remounted as RW (indicates root)
     */
    private fun checkSystemRemounted(): Boolean {
        val systemMounts = listOf(
            "/system",
            "/vendor",
            "/product",
            "/system_ext"
        )

        // Read mount info
        try {
            val bufferedReader = BufferedReader(InputStreamReader(
                File("/proc/mounts").inputStream()
            ))

            bufferedReader.use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    for (mount in systemMounts) {
                        if (line?.contains(mount) == true) {
                            // Check if mounted RW (read-write)
                            if (line?.contains("rw,") == true ||
                                line?.contains(" rw ") == true) {
                                return true
                            }
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
     * Check for dangerous build.prop modifications
     */
    private fun checkForDangerousBuildProps(): Boolean {
        try {
            val buildProps = listOf(
                "/system/build.prop",
                "/vendor/build.prop",
                "/product/build.prop",
                "/system_ext/build.prop"
            )

            for (propFile in buildProps) {
                val file = File(propFile)
                if (file.exists()) {
                    // Check if file is writable (should not be on production)
                    if (file.canWrite()) {
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
     * Get system property value
     */
    private fun getSystemProperty(prop: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $prop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val value = reader.readLine()
            reader.close()
            value
        } catch (e: Exception) {
            null
        }
    }
}

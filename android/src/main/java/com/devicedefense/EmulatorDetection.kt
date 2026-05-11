package com.devicedefense

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import java.io.File

/**
 * Detection for Android emulators
 */
class EmulatorDetection(private val context: Context) {

    /**
     * Check if running on emulator
     */
    fun isEmulator(): Boolean {
        return checkKnownEmulatorProperties() ||
                checkEmulatorBuildProps() ||
                checkEmulatorFiles() ||
                checkEmulatorFeatures() ||
                checkNetworkInterfaces()
    }

    /**
     * Check for known emulator properties
     */
    private fun checkKnownEmulatorProperties(): Boolean {
        val emulatorProps = listOf(
            "generic" to Build.BRAND,
            "generic" to Build.PRODUCT,
            "google_sdk" to Build.PRODUCT,
            "sdk" to Build.PRODUCT,
            "sdk_gphone" to Build.PRODUCT,
            "sdk_gphone64_arm64" to Build.PRODUCT,
            "sdk_gphone_x86" to Build.PRODUCT,
            "sdk_gphone64_x86_64" to Build.PRODUCT,
            "vbox86p" to Build.HARDWARE,
            "vmos" to Build.HARDWARE,
            "nox" to Build.HARDWARE,
            "ttVM_x86" to Build.MANUFACTURER,
            "Genymotion" to Build.MANUFACTURER,
            "Genymotion" to Build.PRODUCT,
            "Android SDK built for x86" to Build.MANUFACTURER,
            "Android SDK built for x86_64" to Build.MANUFACTURER
        )

        for ((key, value) in emulatorProps) {
            if (value.contains(key, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    /**
     * Check emulator build properties
     */
    private fun checkEmulatorBuildProps(): Boolean {
        // Check device model
        val model = Build.MODEL.lowercase()
        if (model.contains("sdk") ||
            model.contains("google_sdk") ||
            model.contains("emulator") ||
            model.contains("android sdk")) {
            return true
        }

        // Check device
        val device = Build.DEVICE.lowercase()
        if (device.contains("generic") ||
            device.contains("emulator") ||
            device.contains("sdk")) {
            return true
        }

        // Check fingerprint
        val fingerprint = Build.FINGERPRINT.lowercase()
        if (fingerprint.contains("generic") ||
            fingerprint.contains("sdk_gphone")) {
            return true
        }

        // Check hardware
        val hardware = Build.HARDWARE.lowercase()
        if (hardware.contains("goldfish") ||
            hardware.contains("ranchu") ||
            hardware.contains("vbox")) {
            return true
        }

        return false
    }

    /**
     * Check for emulator-specific files
     */
    private fun checkEmulatorFiles(): Boolean {
        val emulatorPaths = listOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/system/bin/qemu-props",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd",
            "/system/lib/libnim-dumo.so",
            "/system/lib/libdum.so"
        )

        return emulatorPaths.any { File(it).exists() }
    }

    /**
     * Check for emulator features
     */
    @Suppress("DEPRECATION", "MissingPermission")
    private fun checkEmulatorFeatures(): Boolean {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: return false

            // Check for invalid network operator (only check for known emulator fake values, not null/empty which happens on tablets)
            val networkOperator = telephonyManager.networkOperator
            if (networkOperator == "000000000000000") {
                return true
            }

            // Check for invalid subscriber ID (requires READ_PHONE_STATE, might throw SecurityException)
            try {
                val subscriberId = telephonyManager.subscriberId
                if (subscriberId == "000000000000000") {
                    return true
                }
            } catch (e: SecurityException) {
                // Ignore if permission denied
            }

            // Check for invalid IMEI/MEID (requires READ_PHONE_STATE)
            try {
                val deviceId = telephonyManager.deviceId
                if (deviceId == "000000000000000" || deviceId == "0") {
                    return true
                }
            } catch (e: SecurityException) {
                // Ignore if permission denied
            }
        } catch (e: Exception) {
            // Ignore other telephony errors
        }

        return false
    }

    /**
     * Check network interfaces for emulator indicators
     */
    private fun checkNetworkInterfaces(): Boolean {
        val emulatorInterfaces = listOf(
            "vboxnet",
            "vnic",
            "tun",
            "tap"
        )

        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces.toList()) {
                val name = networkInterface.name.lowercase()
                for (emulatorInterface in emulatorInterfaces) {
                    if (name.contains(emulatorInterface)) {
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
     * Check camera availability (emulators often lack cameras)
     * Note: This is just a hint, many real devices also lack cameras
     */
    private fun checkCamera(): Boolean {
        return try {
            // Check if device has camera hardware feature
            val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                    || context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)

            // If no camera feature, might be emulator (but not guaranteed)
            !hasCamera
        } catch (e: Exception) {
            // On error, assume it's not an emulator based on camera check
            false
        }
    }

    /**
     * Get emulator type if detected
     */
    fun getEmulatorType(): String? {
        return when {
            Build.HARDWARE.contains("ranchu") -> "QEMU/KVM"
            Build.HARDWARE.contains("vbox") -> "VirtualBox"
            Build.MANUFACTURER.contains("Genymotion") -> "Genymotion"
            Build.PRODUCT.contains("nox") -> "Nox"
            Build.HARDWARE.contains("vmos") -> "VMOS"
            Build.BRAND.contains("google") && Build.PRODUCT.contains("sdk") -> "Android Emulator"
            else -> "Unknown"
        }
    }
}

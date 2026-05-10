package vn.osp.security

import android.util.Log
import com.facebook.react.bridge.*
import org.json.JSONObject

/**
 * React Native module for device security detection
 */
class DeviceSecurityModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private val rootDetection by lazy { RootDetection(reactContext) }
    private val hookDetection by lazy { HookDetection(reactContext) }
    private val debugDetection by lazy { DebugDetection(reactContext) }
    private val emulatorDetection by lazy { EmulatorDetection(reactContext) }

    override fun getName(): String = NAME

    override fun getConstants(): Map<String, Any> {
        val nativeLibLoaded = try {
            NativeSecurityCheck.isNativeLibraryLoaded()
        } catch (e: Exception) {
            Log.e(NAME, "Error checking native library", e)
            false
        }

        return mapOf(
            "NAME" to NAME,
            "NATIVE_LIBRARY_LOADED" to nativeLibLoaded
        )
    }

    /**
     * Check if device is rooted (synchronous)
     */
    @ReactMethod(isBlockingSynchronousMethod = true)
    fun isRooted(): Boolean {
        return try {
            val result = rootDetection.performDetection()
            result.isRooted
        } catch (e: Exception) {
            Log.e(NAME, "Error checking root status", e)
            false
        }
    }

    /**
     * Get detailed root detection result
     */
    @ReactMethod
    fun isRootedWithDetails(promise: Promise) {
        try {
            val result = rootDetection.performDetection()
            val json = JSONObject().apply {
                put("isRooted", result.isRooted)
                put("hasRootBeerDetected", result.hasRootBeerDetected)
                put("hasNativeRootDetected", result.hasNativeRootDetected)
                put("hasDangerousBins", result.hasDangerousBins)
                put("hasRootApps", result.hasRootApps)
                put("hasSystemPropsModified", result.hasSystemPropsModified)
                put("details", JSONObject(result.details))
            }
            promise.resolve(json.toString())
        } catch (e: Exception) {
            Log.e(NAME, "Error getting root details", e)
            promise.reject("ROOT_CHECK_ERROR", e.message)
        }
    }

    /**
     * Check if Frida is present
     */
    @ReactMethod(isBlockingSynchronousMethod = true)
    fun hasFrida(): Boolean {
        return try {
            hookDetection.hasFrida()
        } catch (e: Exception) {
            Log.e(NAME, "Error checking Frida", e)
            false
        }
    }

    /**
     * Check if Xposed is present
     */
    @ReactMethod(isBlockingSynchronousMethod = true)
    fun hasXposed(): Boolean {
        return try {
            hookDetection.hasXposed()
        } catch (e: Exception) {
            Log.e(NAME, "Error checking Xposed", e)
            false
        }
    }

    /**
     * Check if Magisk is present
     */
    @ReactMethod(isBlockingSynchronousMethod = true)
    fun hasMagisk(): Boolean {
        return try {
            hookDetection.hasMagisk()
        } catch (e: Exception) {
            Log.e(NAME, "Error checking Magisk", e)
            false
        }
    }

    /**
     * Check if app is debuggable
     */
    @ReactMethod(isBlockingSynchronousMethod = true)
    fun isDebuggable(): Boolean {
        return try {
            debugDetection.isDebuggable()
        } catch (e: Exception) {
            Log.e(NAME, "Error checking debuggable", e)
            false
        }
    }

    /**
     * Check if running on emulator
     */
    @ReactMethod(isBlockingSynchronousMethod = true)
    fun isEmulator(): Boolean {
        return try {
            emulatorDetection.isEmulator()
        } catch (e: Exception) {
            Log.e(NAME, "Error checking emulator", e)
            false
        }
    }

    /**
     * Get comprehensive security status
     */
    @ReactMethod
    fun getSecurityStatus(promise: Promise) {
        try {
            val rootResult = rootDetection.performDetection()
            val hasFrida = hookDetection.hasFrida()
            val hasXposed = hookDetection.hasXposed()
            val hasMagisk = hookDetection.hasMagisk()
            val isDebuggable = debugDetection.isDebuggable()
            val isEmulator = emulatorDetection.isEmulator()

            val threats = mutableListOf<String>()

            if (rootResult.isRooted) {
                threats.add("root_detected")
                if (rootResult.hasRootBeerDetected) threats.add("root_beer_detected")
                if (rootResult.hasNativeRootDetected) threats.add("native_root_detected")
                if (rootResult.hasDangerousBins) threats.add("dangerous_bins_detected")
                if (rootResult.hasRootApps) threats.add("root_apps_detected")
                if (rootResult.hasSystemPropsModified) threats.add("system_props_modified")
            }
            if (hasFrida) threats.add("frida_detected")
            if (hasXposed) threats.add("xposed_detected")
            if (hasMagisk) threats.add("magisk_detected")
            if (isDebuggable) threats.add("debugger_detected")
            if (isEmulator) threats.add("emulator_detected")

            val securityStatus = JSONObject().apply {
                put("isSecure", threats.isEmpty())
                put("threats", org.json.JSONArray(threats))
                put("isRooted", rootResult.isRooted)
                put("hasRootBeerDetected", rootResult.hasRootBeerDetected)
                put("hasNativeRootDetected", rootResult.hasNativeRootDetected)
                put("hasDangerousBins", rootResult.hasDangerousBins)
                put("hasRootApps", rootResult.hasRootApps)
                put("hasSystemPropsModified", rootResult.hasSystemPropsModified)
                put("hasFrida", hasFrida)
                put("hasXposed", hasXposed)
                put("hasMagisk", hasMagisk)
                put("isDebuggable", isDebuggable)
                put("isEmulator", isEmulator)
                put("details", JSONObject().apply {
                    put("emulatorType", emulatorDetection.getEmulatorType())
                    val nativeLibLoaded = try {
                        NativeSecurityCheck.isNativeLibraryLoaded()
                    } catch (e: Exception) {
                        false
                    }
                    put("nativeLibraryLoaded", nativeLibLoaded)
                })
            }

            promise.resolve(securityStatus.toString())
        } catch (e: Exception) {
            Log.e(NAME, "Error getting security status", e)
            promise.reject("SECURITY_STATUS_ERROR", e.message)
        }
    }

    /**
     * Check if device is secure (no threats)
     */
    @ReactMethod
    fun isDeviceSecure(promise: Promise) {
        try {
            val rootResult = rootDetection.performDetection()
            val isSecure = !rootResult.isRooted &&
                    !hookDetection.hasFrida() &&
                    !hookDetection.hasXposed() &&
                    !hookDetection.hasMagisk() &&
                    !debugDetection.isDebuggable() &&
                    !emulatorDetection.isEmulator()

            promise.resolve(isSecure)
        } catch (e: Exception) {
            Log.e(NAME, "Error checking device security", e)
            promise.reject("SECURITY_CHECK_ERROR", e.message)
        }
    }

    /**
     * Block app when security threat detected
     * Shows an alert and exits the app
     */
    @ReactMethod
    fun blockOnSecurityThreat(
        showAlert: Boolean,
        alertTitle: String,
        alertMessage: String,
        alertButtonText: String
    ) {
        try {
            val rootResult = rootDetection.performDetection()
            val hasFrida = hookDetection.hasFrida()
            val hasXposed = hookDetection.hasXposed()
            val hasMagisk = hookDetection.hasMagisk()
            val isDebuggable = debugDetection.isDebuggable()
            val isEmulator = emulatorDetection.isEmulator()

            val hasThreat = rootResult.isRooted ||
                    hasFrida ||
                    hasXposed ||
                    hasMagisk ||
                    isDebuggable ||
                    isEmulator

            if (hasThreat) {
                Log.w(NAME, "Security threat detected, blocking app")

                // Show alert if requested
                if (showAlert) {
                    val activity = currentActivity
                    activity?.runOnUiThread {
                        android.app.AlertDialog.Builder(activity)
                            .setCancelable(false)
                            .setTitle(alertTitle)
                            .setMessage(alertMessage)
                            .setPositiveButton(alertButtonText) { _, _ ->
                                activity.finishAffinity()
                                System.exit(0)
                            }
                            .show()
                    }
                } else {
                    // Exit without alert
                    currentActivity?.finishAffinity()
                    System.exit(0)
                }
            } else {
                Log.i(NAME, "No security threat detected, app continues")
            }
        } catch (e: Exception) {
            Log.e(NAME, "Error blocking on security threat", e)
        }
    }

    companion object {
        const val NAME = "DeviceSecurity"
    }
}

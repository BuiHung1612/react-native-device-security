package com.devicedefense

/**
 * Native (JNI) security checks
 * This class provides native methods that are harder to bypass with JavaScript hooks
 */
object NativeSecurityCheck {

    init {
        try {
            System.loadLibrary("device-security")
        } catch (e: UnsatisfiedLinkError) {
            // Native library not loaded, fallback to Java/Kotlin checks
        }
    }

    /**
     * Native root detection
     * Uses C++ code to check for root indicators that are harder to bypass
     * @return true if device is rooted
     */
    external fun isRooted(): Boolean

    /**
     * Native check for dangerous binaries
     * @return true if dangerous binaries found
     */
    external fun hasDangerousBinaries(): Boolean

    /**
     * Native check for system properties
     * @return true if suspicious system properties found
     */
    external fun hasSuspiciousSystemProperties(): Boolean

    /**
     * Native check for hook frameworks
     * @return true if hooking framework detected
     */
    external fun hasHookFramework(): Boolean

    /**
     * Native check for debugger
     * @return true if debugger detected
     */
    external fun isDebuggerAttached(): Boolean

    /**
     * Get native security status as JSON string
     * @return JSON string with security status
     */
    external fun getSecurityStatus(): String

    /**
     * Check if native library is loaded
     */
    fun isNativeLibraryLoaded(): Boolean {
        return try {
            // Try to call a native method
            isRooted()
            true
        } catch (e: UnsatisfiedLinkError) {
            false
        }
    }
}

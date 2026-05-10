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
     * Native check for SSL validation bypass
     * Checks if SSL certificate validation has been bypassed
     * @return true if SSL validation is bypassed
     */
    external fun hasSSLValidationBypass(): Boolean

    /**
     * Native check for SSL pinning bypass tools
     * Checks for common SSL pinning bypass frameworks and tools
     * @return true if SSL pinning bypass detected
     */
    external fun hasSSLPinningBypass(): Boolean

    /**
     * Native check for proxy configuration
     * Checks if HTTP/HTTPS proxy is configured (potential MITM)
     * @return true if proxy is configured
     */
    external fun hasProxyConfiguration(): Boolean

    /**
     * Native check for modified SSL libraries
     * Checks if SSL libraries are from unexpected locations
     * @return true if modified SSL libraries detected
     */
    external fun hasModifiedSSLLibraries(): Boolean

    /**
     * Native check for certificate tampering
     * Checks for excessive user-installed CA certificates
     * @return true if certificate tampering detected
     */
    external fun hasCertificateTampering(): Boolean

    /**
     * Native comprehensive SSL security check
     * Checks all SSL security aspects
     * @return true if any SSL security issue detected
     */
    external fun hasSSLSecurityIssue(): Boolean

    /**
     * Get native security status as JSON string
     * @return JSON string with security status including SSL checks
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

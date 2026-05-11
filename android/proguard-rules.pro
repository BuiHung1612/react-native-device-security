# React Native Device Defense - ProGuard/R8 Rules

# Keep all native methods
-keepclassmembers class com.devicedefense.** {
    native <methods>;
}

# Keep the entire module
-keep class com.devicedefense.** { *; }
-keep interface com.devicedefense.** { *; }

# Keep enum classes
-keepclassmembers enum com.devicedefense.** {
    *[];
}

# Don't warn about missing classes
-dontwarn com.devicedefense.**

# Obfuscate but keep critical method names for native calls
-keep class com.devicedefense.NativeSecurityCheck {
    public static boolean isRooted();
    public static boolean hasDangerousBinaries();
    public static boolean hasSuspiciousSystemProperties();
    public static boolean hasHookFramework();
    public static boolean isDebuggerAttached();
    public static boolean hasSSLValidationBypass();
    public static boolean hasSSLPinningBypass();
    public static boolean hasProxyConfiguration();
    public static boolean hasModifiedSSLLibraries();
    public static boolean hasCertificateTampering();
    public static boolean hasSSLSecurityIssue();
    public static java.lang.String getSecurityStatus();
}

# React Native Device Security - ProGuard/R8 Rules

# Keep all native methods
-keepclassmembers class vn.osp.security.** {
    native <methods>;
}

# Keep the entire module
-keep class vn.osp.security.** { *; }
-keep interface vn.osp.security.** { *; }

# Keep enum classes
-keepclassmembers enum vn.osp.security.** {
    *[];
}

# Don't warn about missing classes
-dontwarn vn.osp.security.**

# Obfuscate but keep critical method names for native calls
-keep class vn.osp.security.NativeSecurityCheck {
    public static boolean isRooted();
    public static boolean hasDangerousBinaries();
    public static boolean hasSuspiciousSystemProperties();
    public static boolean hasHookFramework();
    public static boolean isDebuggerAttached();
    public static java.lang.String getSecurityStatus();
}

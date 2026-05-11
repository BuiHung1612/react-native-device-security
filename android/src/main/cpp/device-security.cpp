#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <android/log.h>
#include <stdexcept>


#define LOG_TAG "DeviceSecurityNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 * Check if a file exists
 */
static bool fileExists(const std::string& path) {
    struct stat buffer;
    return (stat(path.c_str(), &buffer) == 0);
}

/**
 * Check if a file is executable
 */
static bool isExecutable(const std::string& path) {
    struct stat buffer;
    if (stat(path.c_str(), &buffer) != 0) {
        return false;
    }
    return (buffer.st_mode & S_IXUSR) != 0;
}

/**
 * Check if a directory exists
 */
static bool directoryExists(const std::string& path) {
    struct stat buffer;
    return (stat(path.c_str(), &buffer) == 0) && S_ISDIR(buffer.st_mode);
}

/**
 * Check for su binary in various locations
 */
static bool checkSuBinary() {
    const std::vector<std::string> suPaths = {
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/magisk/.core/bin/su",
        "/system/usr/we-need-root/su"
    };

    for (const auto& path : suPaths) {
        if (fileExists(path) && isExecutable(path)) {
            LOGD("Found su binary at: %s", path.c_str());
            return true;
        }
    }

    return false;
}

/**
 * Check for dangerous binaries
 */
static bool checkDangerousBinaries() {
    const std::vector<std::string> binaries = {
        "/system/xbin/busybox",
        "/system/bin/busybox",
        "/sbin/busybox",
        "/data/local/xbin/busybox",
        "/data/local/bin/busybox",
        "/system/app/Superuser.apk",
        "/sbin/.magisk",
        "/system/xbin/magisk",
        "/system/bin/magisk"
    };

    for (const auto& binary : binaries) {
        if (fileExists(binary)) {
            LOGD("Found dangerous binary: %s", binary.c_str());
            return true;
        }
    }

    return false;
}

/**
 * Check for root-related directories
 */
static bool checkRootDirectories() {
    const std::vector<std::string> directories = {
        "/data/local/xbin",
        "/data/local/bin",
        "/su",
        "/system/app/SuperSU",
        "/system/app/Superuser",
        "/data/data/com.noshufou.android.su",
        "/data/data/com.thirdparty.superuser",
        "/data/data/eu.chainfire.supersu",
        "/data/data/com.topjohnwu.magisk",
        "/cache/magisk.log",
        "/dev/com.topjohnwu.magisk",
        "/system/app/MagiskManager",
        "/data/adb/magisk",
        "/data/adb/modules"
    };

    for (const auto& dir : directories) {
        if (directoryExists(dir)) {
            LOGD("Found root directory: %s", dir.c_str());
            return true;
        }
    }

    return false;
}

/**
 * Check system properties for root indicators
 */
static bool checkSystemProperties() {
    const std::vector<std::string> propFiles = {
        "/default.prop",
        "/system/build.prop",
        "/vendor/build.prop",
        "/product/build.prop"
    };

    for (const auto& propFile : propFiles) {
        if (!fileExists(propFile)) continue;

        std::ifstream file(propFile);
        std::string line;

        while (std::getline(file, line)) {
            if (line.find("ro.debuggable=1") != std::string::npos) {
                LOGD("Found ro.debuggable=1 in %s", propFile.c_str());
                return true;
            }
            if (line.find("ro.secure=0") != std::string::npos) {
                LOGD("Found ro.secure=0 in %s", propFile.c_str());
                return true;
            }
            if (line.find("service.adb.root=1") != std::string::npos) {
                LOGD("Found service.adb.root=1 in %s", propFile.c_str());
                return true;
            }
        }
    }

    return false;
}

/**
 * Check mount points for RW system
 */
static bool checkMountPoints() {
    std::ifstream mounts("/proc/mounts");
    std::string line;

    const std::vector<std::string> systemMounts = {
        "/system",
        "/vendor",
        "/product",
        "/system_ext"
    };

    while (std::getline(mounts, line)) {
        // Check if any system mount is mounted RW
        for (const auto& mount : systemMounts) {
            if (line.find(mount) != std::string::npos) {
                if (line.find("rw,") != std::string::npos ||
                    line.find(" rw ") != std::string::npos) {
                    LOGD("Found RW mount: %s", line.c_str());
                    return true;
                }
            }
        }
    }

    return false;
}

/**
 * Check for tracer PID (debugger detection)
 */
static bool checkTracerPid() {
    std::ifstream statusFile("/proc/self/status");
    std::string line;

    while (std::getline(statusFile, line)) {
        if (line.find("TracerPid:") == 0) {
            try {
                size_t pos = line.find_last_not_of(" \t\n\r");
                if (pos != std::string::npos && pos > 10) {
                    std::string value = line.substr(10, pos - 9);
                    int tracerPid = std::stoi(value);
                    if (tracerPid > 0) {
                        LOGD("Tracer PID detected: %d", tracerPid);
                        return true;
                    }
                }
            } catch (const std::exception& e) {
                // Parse error, ignore
            }
        }
    }

    return false;
}

/**
 * Check for Frida in /proc/self/maps
 */
static bool checkFridaInMaps() {
    std::ifstream mapsFile("/proc/self/maps");
    std::string line;

    while (std::getline(mapsFile, line)) {
        if (line.find("frida") != std::string::npos ||
            line.find("frida-agent") != std::string::npos ||
            line.find("libfrida") != std::string::npos) {
            LOGD("Found Frida in maps: %s", line.c_str());
            return true;
        }
    }

    return false;
}

/**
 * Check for SSL validation bypass in system properties
 */
static bool checkSSLValidationBypass() {
    const std::vector<std::string> propFiles = {
        "/system/build.prop",
        "/vendor/build.prop",
        "/default.prop"
    };

    for (const auto& propFile : propFiles) {
        if (!fileExists(propFile)) continue;

        std::ifstream file(propFile);
        std::string line;

        while (std::getline(file, line)) {
            // Check for SSL validation bypass indicators
            if (line.find("ssl.untrusted=0") != std::string::npos) {
                LOGD("Found SSL validation bypass in %s", propFile.c_str());
                return true;
            }
            if (line.find("ro.debuggable") != std::string::npos &&
                line.find("1") != std::string::npos) {
                // Debuggable builds may have SSL validation bypassed
                LOGD("Device is debuggable, SSL may be bypassed");
                return true;
            }
        }
    }

    return false;
}

/**
 * Check for common SSL pinning bypass tools and frameworks
 */
static bool checkSSLPinningBypass() {
    // Check for known SSL pinning bypass tools in /proc/self/maps
    std::ifstream mapsFile("/proc/self/maps");
    std::string line;

    const std::vector<std::string> bypassLibraries = {
        "libssl-bypass",
        " Frida",
        "xposed",
        "substrate",
        "magisk",
        "r0puse",
        "ssl-pin bypass",
        "trustmekit"
    };

    while (std::getline(mapsFile, line)) {
        std::string lowerLine = line;
        std::transform(lowerLine.begin(), lowerLine.end(), lowerLine.begin(), ::tolower);

        for (const auto& lib : bypassLibraries) {
            if (lowerLine.find(lib) != std::string::npos) {
                LOGD("Found SSL pinning bypass tool: %s", lib.c_str());
                return true;
            }
        }
    }

    // Check for common SSL bypass apps
    const std::vector<std::string> bypassApps = {
        "/data/data/de.robv.android.xposed.installer",
        "/data/data/com.sensei.withakemon",
        "/data/data/com.solid.pinkyscan",
        "/data/data/jp.co.cyberagent.android.deviceauthorization"
    };

    for (const auto& app : bypassApps) {
        if (directoryExists(app)) {
            LOGD("Found SSL bypass app: %s", app.c_str());
            return true;
        }
    }

    return false;
}

/**
 * Check for proxy configuration that could intercept SSL traffic
 */
static bool checkProxyConfiguration() {
    // Check for HTTP proxy in system properties
    const std::vector<std::string> propFiles = {
        "/system/build.prop",
        "/vendor/build.prop"
    };

    for (const auto& propFile : propFiles) {
        if (!fileExists(propFile)) continue;

        std::ifstream file(propFile);
        std::string line;

        while (std::getline(file, line)) {
            if (line.find("http.proxy") != std::string::npos ||
                line.find("https.proxy") != std::string::npos) {
                LOGD("Found proxy configuration in %s", propFile.c_str());
                return true;
            }
        }
    }

    // Check for proxy environment variables
    if (getenv("http_proxy") != nullptr || getenv("https_proxy") != nullptr) {
        LOGD("Found proxy environment variables");
        return true;
    }

    return false;
}

/**
 * Check for modified SSL libraries
 */
static bool checkModifiedSSLLibraries() {
    // Check if SSL libraries are from unexpected locations
    std::ifstream mapsFile("/proc/self/maps");
    std::string line;

    const std::vector<std::string> trustedPaths = {
        "/system/lib/libssl",
        "/system/lib64/libssl",
        "/apex/com.android.conscrypt/lib",
        "/data/app/", // App's own lib path
        "/com.android.conscrypt"
    };

    while (std::getline(mapsFile, line)) {
        if (line.find("libssl") != std::string::npos ||
            line.find("libcrypto") != std::string::npos) {

            // Check if library is from trusted path
            bool fromTrustedPath = false;
            for (const auto& trusted : trustedPaths) {
                if (line.find(trusted) != std::string::npos) {
                    fromTrustedPath = true;
                    break;
                }
            }

            if (!fromTrustedPath && line.find("r-xp") != std::string::npos) {
                // Library is executable but not from trusted path
                LOGD("Found potentially modified SSL library: %s", line.c_str());
                return true;
            }
        }
    }

    return false;
}

/**
 * Check for certificate tampering
 */
static bool checkCertificateTampering() {
    // Check for user-installed CA certificates
    const std::vector<std::string> certPaths = {
        "/data/misc/keychain/cacerts-added",
        "/system/etc/security/cacerts"
    };

    for (const auto& certPath : certPaths) {
        if (directoryExists(certPath)) {
            DIR* dir = opendir(certPath.c_str());
            if (dir != nullptr) {
                struct dirent* entry;
                int certCount = 0;

                while ((entry = readdir(dir)) != nullptr) {
                    if (entry->d_type == DT_REG) {
                        certCount++;
                    }
                }

                closedir(dir);

                // Too many user certificates might indicate tampering
                if (certCount > 100) {
                    LOGD("Suspicious number of certificates: %d", certCount);
                    return true;
                }
            }
        }
    }

    return false;
}

/**
 * Comprehensive SSL security check
 */
static bool checkSSLSecurity() {
    bool hasIssue = false;

    if (checkSSLValidationBypass()) {
        LOGD("SSL validation bypass detected");
        hasIssue = true;
    }

    if (checkSSLPinningBypass()) {
        LOGD("SSL pinning bypass detected");
        hasIssue = true;
    }

    if (checkProxyConfiguration()) {
        LOGD("Proxy configuration detected");
        hasIssue = true;
    }

    if (checkModifiedSSLLibraries()) {
        LOGD("Modified SSL libraries detected");
        hasIssue = true;
    }

    if (checkCertificateTampering()) {
        LOGD("Certificate tampering detected");
        hasIssue = true;
    }

    return hasIssue;
}

/**
 * Main root detection function
 */
static bool performRootDetection() {
    // Check for su binary
    if (checkSuBinary()) {
        return true;
    }

    // Check for dangerous binaries
    if (checkDangerousBinaries()) {
        return true;
    }

    // Check for root directories
    if (checkRootDirectories()) {
        return true;
    }

    // Check system properties
    if (checkSystemProperties()) {
        return true;
    }

    // Check mount points
    if (checkMountPoints()) {
        return true;
    }

    return false;
}

// Updated JNI functions with new package name
extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_isRooted(JNIEnv* env, jobject /* this */) {
    return performRootDetection() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasDangerousBinaries(JNIEnv* env, jobject /* this */) {
    return checkDangerousBinaries() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasSuspiciousSystemProperties(JNIEnv* env, jobject /* this */) {
    return checkSystemProperties() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasHookFramework(JNIEnv* env, jobject /* this */) {
    return checkFridaInMaps() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_isDebuggerAttached(JNIEnv* env, jobject /* this */) {
    return checkTracerPid() ? JNI_TRUE : JNI_FALSE;
}

// New SSL security functions
extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasSSLValidationBypass(JNIEnv* env, jobject /* this */) {
    return checkSSLValidationBypass() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasSSLPinningBypass(JNIEnv* env, jobject /* this */) {
    return checkSSLPinningBypass() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasProxyConfiguration(JNIEnv* env, jobject /* this */) {
    return checkProxyConfiguration() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasModifiedSSLLibraries(JNIEnv* env, jobject /* this */) {
    return checkModifiedSSLLibraries() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasCertificateTampering(JNIEnv* env, jobject /* this */) {
    return checkCertificateTampering() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_devicedefense_NativeSecurityCheck_hasSSLSecurityIssue(JNIEnv* env, jobject /* this */) {
    return checkSSLSecurity() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_devicedefense_NativeSecurityCheck_getSecurityStatus(JNIEnv* env, jobject /* this */) {
    bool isRooted = performRootDetection();
    bool hasDangerousBins = checkDangerousBinaries();
    bool hasSuspiciousProps = checkSystemProperties();
    bool hasHook = checkFridaInMaps();
    bool hasDebugger = checkTracerPid();
    bool hasSSLIssue = checkSSLSecurity();
    bool hasSSLBypass = checkSSLPinningBypass();
    bool hasProxy = checkProxyConfiguration();
    bool hasModifiedSSL = checkModifiedSSLLibraries();
    bool hasCertTampering = checkCertificateTampering();

    std::string json = "{";
    json += "\"isRooted\":" + std::string(isRooted ? "true" : "false") + ",";
    json += "\"hasDangerousBins\":" + std::string(hasDangerousBins ? "true" : "false") + ",";
    json += "\"hasSuspiciousProps\":" + std::string(hasSuspiciousProps ? "true" : "false") + ",";
    json += "\"hasHookFramework\":" + std::string(hasHook ? "true" : "false") + ",";
    json += "\"isDebuggerAttached\":" + std::string(hasDebugger ? "true" : "false") + ",";
    json += "\"hasSSLSecurityIssue\":" + std::string(hasSSLIssue ? "true" : "false") + ",";
    json += "\"hasSSLValidationBypass\":" + std::string(hasSSLBypass ? "true" : "false") + ",";
    json += "\"hasProxyConfiguration\":" + std::string(hasProxy ? "true" : "false") + ",";
    json += "\"hasModifiedSSLLibraries\":" + std::string(hasModifiedSSL ? "true" : "false") + ",";
    json += "\"hasCertificateTampering\":" + std::string(hasCertTampering ? "true" : "false");
    json += "}";

    return env->NewStringUTF(json.c_str());
}

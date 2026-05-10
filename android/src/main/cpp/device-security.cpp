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

extern "C" JNIEXPORT jboolean JNICALL
Java_vn_osp_security_NativeSecurityCheck_isRooted(JNIEnv* env, jobject /* this */) {
    return performRootDetection() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_vn_osp_security_NativeSecurityCheck_hasDangerousBinaries(JNIEnv* env, jobject /* this */) {
    return checkDangerousBinaries() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_vn_osp_security_NativeSecurityCheck_hasSuspiciousSystemProperties(JNIEnv* env, jobject /* this */) {
    return checkSystemProperties() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_vn_osp_security_NativeSecurityCheck_hasHookFramework(JNIEnv* env, jobject /* this */) {
    return checkFridaInMaps() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_vn_osp_security_NativeSecurityCheck_isDebuggerAttached(JNIEnv* env, jobject /* this */) {
    return checkTracerPid() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_vn_osp_security_NativeSecurityCheck_getSecurityStatus(JNIEnv* env, jobject /* this */) {
    bool isRooted = performRootDetection();
    bool hasDangerousBins = checkDangerousBinaries();
    bool hasSuspiciousProps = checkSystemProperties();
    bool hasHook = checkFridaInMaps();
    bool hasDebugger = checkTracerPid();

    std::string json = "{";
    json += "\"isRooted\":" + std::string(isRooted ? "true" : "false") + ",";
    json += "\"hasDangerousBins\":" + std::string(hasDangerousBins ? "true" : "false") + ",";
    json += "\"hasSuspiciousProps\":" + std::string(hasSuspiciousProps ? "true" : "false") + ",";
    json += "\"hasHookFramework\":" + std::string(hasHook ? "true" : "false") + ",";
    json += "\"isDebuggerAttached\":" + std::string(hasDebugger ? "true" : "false");
    json += "}";

    return env->NewStringUTF(json.c_str());
}

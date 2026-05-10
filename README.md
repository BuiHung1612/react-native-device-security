# react-native-device-defense

> Multi-layer device security detection for React Native - Root detection, Anti-hook, Anti-debug, Emulator detection

## Features

- ✅ **Multi-layer Root Detection** - Detect rooted devices using multiple techniques
- 🔒 **Native C++ Detection** - Harder to bypass with JavaScript hooks
- 🎣 **Frida/Xposed Detection** - Detect common hooking frameworks
- 🐛 **Anti-Debug** - Detect debugger attachment
- 📱 **Emulator Detection** - Detect Android emulators
- 🔐 **SSL Pinning Detection** - Native C++ SSL security checks to prevent MITM attacks
- 🛡️ **App Integrity Check** - Verify app signature and tampering
- 🔐 **Block on Security Threat** - Automatically block app when security issues detected

## Installation

```bash
npm install react-native-device-defense
# or
yarn add react-native-device-defense
```

## Android Setup

### React Native 0.60+ (Autolinking)

No manual setup required! Just install and rebuild:

```bash
yarn add react-native-device-defense
npx react-native run-android
```

### React Native < 0.60 (Manual Linking)

1. Add to `android/settings.gradle`:

```gradle
include ':react-native-device-defense'
project(':react-native-device-defense').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-device-defense/android')
```

2. Add to `android/app/build.gradle`:

```gradle
dependencies {
    implementation project(':react-native-device-defense')
}
```

3. Add to `MainApplication.java`:

```java
import com.devicedefense.DeviceSecurityPackage;

@Override
protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
        // ... other packages
        new DeviceSecurityPackage()
    );
}
```

## Usage

### Basic Usage

```typescript
import DeviceSecurity from 'react-native-device-defense';

// Check if device is secure
const isSecure = await DeviceSecurity.isDeviceSecure();

if (!isSecure) {
  // Device is rooted, has hooks, or other security issues
  Alert.alert(
    'Security Warning',
    'This device is not secure. The app cannot run on rooted or modified devices.',
    [{ text: 'OK', onPress: () => BackHandler.exitApp() }]
  );
}
```

### Advanced Usage with Hook

```typescript
import { useDeviceSecurity } from 'react-native-device-defense';

function App() {
  const { isSecure, securityStatus, isLoading } = useDeviceSecurity({
    onSecurityThreat: (threat) => {
      console.log('Security threat detected:', threat);
      // Handle security threat - block app, show alert, etc.
    },
    blockOnThreat: true, // Block app when security threat detected
  });

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (!isSecure) {
    return <SecurityBlockedScreen threats={securityStatus.threats} />;
  }

  return <MainApp />;
}
```

### Security Status Details

```typescript
const status = await DeviceSecurity.getSecurityStatus();

console.log({
  isSecure: status.isSecure,
  isRooted: status.isRooted,
  hasRootBeerDetected: status.hasRootBeerDetected,
  hasNativeRootDetected: status.hasNativeRootDetected,
  hasDangerousBins: status.hasDangerousBins,
  hasRootApps: status.hasRootApps,
  hasSystemPropsModified: status.hasSystemPropsModified,
  hasFrida: status.hasFrida,
  hasXposed: status.hasXposed,
  hasMagisk: status.hasMagisk,
  isDebuggable: status.isDebuggable,
  isEmulator: status.isEmulator,
});
```

### Block on Security Threat (Recommended for Production)

```typescript
import DeviceSecurity from 'react-native-device-defense';

// In your app entry point
DeviceSecurity.blockOnSecurityThreat({
  showAlert: true,
  alertTitle: 'Cảnh báo bảo mật',
  alertMessage: 'Thiết bị của bạn không an toàn. Ứng dụng không thể chạy trên thiết bị đã root hoặc có sửa đổi.',
  onBlocked: () => {
    // Optional callback when app is blocked
    console.log('App blocked due to security threat');
  }
});
```

### SSL Security Check

Prevent MITM (Man-in-the-Middle) attacks with native SSL security checks:

```typescript
import DeviceSecurity from 'react-native-device-defense';

// Quick SSL security check
const hasSSLIssue = DeviceSecurity.hasSSLSecurityIssue();

if (hasSSLIssue) {
  console.log('SSL security issue detected!');
  // Block sensitive operations or show warning
}

// Detailed SSL security status
const sslStatus = await DeviceSecurity.getSSLSecurityStatus();

console.log({
  hasSSLValidationBypass: sslStatus.hasSSLValidationBypass,
  hasSSLPinningBypass: sslStatus.hasSSLPinningBypass,
  hasProxyConfiguration: sslStatus.hasProxyConfiguration, // Potential MITM
  hasModifiedSSLLibraries: sslStatus.hasModifiedSSLLibraries,
  hasCertificateTampering: sslStatus.hasCertificateTampering,
});

// Check for specific SSL threats
if (DeviceSecurity.hasSSLPinningBypass()) {
  // SSL pinning bypass tools detected (Frida, Xposed, etc.)
  Alert.alert('Security Warning', 'SSL pinning bypass detected. Your connection may not be secure.');
}

if (DeviceSecurity.hasProxyConfiguration()) {
  // Proxy configured - potential MITM attack
  console.warn('Proxy configuration detected - possible MITM');
}

if (DeviceSecurity.hasCertificateTampering()) {
  // Excessive user certificates detected
  Alert.alert('Security Warning', 'Device certificates have been modified.');
}
```

## API Reference

### Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `isDeviceSecure()` | `Promise<boolean>` | Check if device is secure (no threats) |
| `getSecurityStatus()` | `Promise<SecurityStatus>` | Get detailed security status |
| `getSSLSecurityStatus()` | `Promise<SSLSecurityStatus>` | Get detailed SSL security status |
| `blockOnSecurityThreat(options)` | `void` | Block app when security threat detected |
| `isRooted()` | `boolean` | Check if device is rooted (synchronous) |
| `hasFrida()` | `boolean` | Check if Frida is present |
| `hasXposed()` | `boolean` | Check if Xposed framework is present |
| `hasMagisk()` | `boolean` | Check if Magisk is present |
| `isDebuggable()` | `boolean` | Check if app is debuggable |
| `isEmulator()` | `boolean` | Check if running on emulator |
| `hasSSLValidationBypass()` | `boolean` | Check if SSL validation is bypassed |
| `hasSSLPinningBypass()` | `boolean` | Check for SSL pinning bypass tools |
| `hasProxyConfiguration()` | `boolean` | Check if proxy is configured (MITM risk) |
| `hasModifiedSSLLibraries()` | `boolean` | Check if SSL libraries are modified |
| `hasCertificateTampering()` | `boolean` | Check for certificate tampering |
| `hasSSLSecurityIssue()` | `boolean` | Comprehensive SSL security check |

### Types

```typescript
interface SecurityStatus {
  isSecure: boolean;
  threats: SecurityThreat[];
  isRooted: boolean;
  hasRootBeerDetected: boolean;
  hasNativeRootDetected: boolean;
  hasDangerousBins: boolean;
  hasRootApps: boolean;
  hasSystemPropsModified: boolean;
  hasFrida: boolean;
  hasXposed: boolean;
  hasMagisk: boolean;
  isDebuggable: boolean;
  isEmulator: boolean;
  // SSL security fields
  hasSSLValidationBypass: boolean;
  hasSSLPinningBypass: boolean;
  hasProxyConfiguration: boolean;
  hasModifiedSSLLibraries: boolean;
  hasCertificateTampering: boolean;
  hasSSLSecurityIssue: boolean;
}

interface SSLSecurityStatus {
  hasSSLValidationBypass: boolean;
  hasSSLPinningBypass: boolean;
  hasProxyConfiguration: boolean;
  hasModifiedSSLLibraries: boolean;
  hasCertificateTampering: boolean;
  hasSSLSecurityIssue: boolean;
}

type SecurityThreat =
  | 'root_detected'
  | 'frida_detected'
  | 'xposed_detected'
  | 'magisk_detected'
  | 'debugger_detected'
  | 'emulator_detected'
  | 'system_props_modified'
  | 'ssl_validation_bypass'
  | 'ssl_pinning_bypass'
  | 'proxy_configuration'
  | 'modified_ssl_libraries'
  | 'certificate_tampering';
```

## Configuration

### ProGuard/R8 Rules

Add to `android/app/proguard-rules.pro`:

```proguard
# Device Security Library
-keep class com.devicedefense.** { *; }
-keepclassmembers class com.devicedefense.** { *; }
-dontwarn com.devicedefense.**
```

## Security Techniques

### Root Detection
- RootBeer library checks
- Native C++ checks (JNI)
- System properties (`ro.debuggable`, `ro.secure`)
- Dangerous binaries (`su`, `busybox`, `magisk`)
- Root management apps detection
- Mount point checks (`/system`, `/vendor` RW)

### Hook Detection
- Frida port scanning (27042, 27043)
- Frida library detection
- Xposed framework detection
- Magisk module detection

### Anti-Debug
- `Debug.isDebuggerConnected()` check
- Tracer PID in `/proc/self/status`
- Timing analysis

### Emulator Detection
- Known emulator properties
- Generic device features
- Genymotion, Nox, BlueStacks detection

### SSL Security Detection (Native C++)
- SSL validation bypass detection
- SSL pinning bypass tools detection (Frida, Xposed, Substrate)
- Proxy configuration detection (MITM risk)
- Modified SSL libraries detection
- Certificate tampering detection
- User-installed CA certificate monitoring

## License

MIT

## Contributing

Pull requests are welcome!

## Support

For issues and questions, please open a GitHub issue at https://github.com/BuiHung1612/react-native-device-security/issues

# react-native-device-security

> Multi-layer device security detection for React Native - Root detection, Anti-hook, Anti-debug, Emulator detection

## Features

- ✅ **Multi-layer Root Detection** - Detect rooted devices using multiple techniques
- 🔒 **Native C++ Detection** - Harder to bypass with JavaScript hooks
- 🎣 **Frida/Xposed Detection** - Detect common hooking frameworks
- 🐛 **Anti-Debug** - Detect debugger attachment
- 📱 **Emulator Detection** - Detect Android emulators
- 🛡️ **App Integrity Check** - Verify app signature and tampering
- 🔐 **Block on Security Threat** - Automatically block app when security issues detected

## Installation

```bash
npm install react-native-device-security
# or
yarn add react-native-device-security
```

## Android Setup

1. Add to `android/settings.gradle`:

```gradle
include ':react-native-device-security'
project(':react-native-device-security').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-device-security/android')
```

2. Add to `android/app/build.gradle`:

```gradle
dependencies {
    implementation project(':react-native-device-security')
}
```

3. Add to `MainApplication.java`:

```java
import vn.osp.security.DeviceSecurityPackage;

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
import DeviceSecurity from 'react-native-device-security';

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
import { useDeviceSecurity } from 'react-native-device-security';

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
import DeviceSecurity from 'react-native-device-security';

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

## API Reference

### Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `isDeviceSecure()` | `Promise<boolean>` | Check if device is secure (no threats) |
| `getSecurityStatus()` | `Promise<SecurityStatus>` | Get detailed security status |
| `blockOnSecurityThreat(options)` | `void` | Block app when security threat detected |
| `isRooted()` | `boolean` | Check if device is rooted (synchronous) |
| `hasFrida()` | `boolean` | Check if Frida is present |
| `hasXposed()` | `boolean` | Check if Xposed framework is present |
| `hasMagisk()` | `boolean` | Check if Magisk is present |
| `isDebuggable()` | `boolean` | Check if app is debuggable |
| `isEmulator()` | `boolean` | Check if running on emulator |

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
}

type SecurityThreat =
  | 'root_detected'
  | 'frida_detected'
  | 'xposed_detected'
  | 'magisk_detected'
  | 'debugger_detected'
  | 'emulator_detected'
  | 'system_props_modified';
```

## Configuration

### ProGuard/R8 Rules

Add to `android/app/proguard-rules.pro`:

```proguard
# Device Security Library
-keep class vn.osp.security.** { *; }
-keepclassmembers class vn.osp.security.** { *; }
-dontwarn vn.osp.security.**
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

## License

MIT

## Author

OSP <dev@osp.vn>

## Contributing

Pull requests are welcome!

## Support

For issues and questions, please open a GitHub issue.

# Usage Examples

## Example 1: Basic Security Check

```typescript
import DeviceSecurity from 'react-native-device-security';

// Check if device is secure
const isSecure = await DeviceSecurity.isDeviceSecure();
console.log('Device is secure:', isSecure);

// Get detailed status
const status = await DeviceSecurity.getSecurityStatus();
console.log('Security status:', status);
```

## Example 2: Using the Hook

```typescript
import { useDeviceSecurity, SecurityBlockedScreen } from 'react-native-device-security';

function MyAppComponent() {
  const { isSecure, isLoading, securityStatus } = useDeviceSecurity({
    onSecurityThreat: (threat, status) => {
      console.log('Threat detected:', threat);
      // Send to analytics or backend
    },
    blockOnThreat: false, // Don't auto-block, handle manually
  });

  if (isLoading) {
    return <LoadingView />;
  }

  if (!isSecure) {
    return (
      <SecurityBlockedScreen
        threats={securityStatus?.threats}
        title="Security Alert"
        message="Your device is not secure"
      />
    );
  }

  return <MainApp />;
}
```

## Example 3: Auto-Block on Threat

```typescript
import DeviceSecurity from 'react-native-device-security';

// In your app root
useEffect(() => {
  DeviceSecurity.blockOnSecurityThreat({
    showAlert: true,
    alertTitle: 'Cảnh báo bảo mật',
    alertMessage: 'Thiết bị của bạn không an toàn. Ứng dụng sẽ thoát.',
    alertButtonText: 'Đóng',
    onBlocked: (status) => {
      // Log to analytics
      Analytics.log('security_blocked', { threats: status.threats });
    },
  });
}, []);
```

## Example 4: Individual Checks

```typescript
import DeviceSecurity from 'react-native-device-security';

// Check specific threats
const isRooted = DeviceSecurity.isRooted();
const hasFrida = DeviceSecurity.hasFrida();
const hasXposed = DeviceSecurity.hasXposed();
const hasMagisk = DeviceSecurity.hasMagisk();
const isDebuggable = DeviceSecurity.isDebuggable();
const isEmulator = DeviceSecurity.isEmulator();

console.log({
  isRooted,
  hasFrida,
  hasXposed,
  hasMagisk,
  isDebuggable,
  isEmulator,
});
```

## Example 5: Custom Blocking Logic

```typescript
import { useDeviceSecurity } from 'react-native-device-security';

function App() {
  const { isSecure, securityStatus } = useDeviceSecurity({
    onSecurityThreat: (threat, status) => {
      // Custom handling based on threat type
      switch (threat) {
        case 'emulator_detected':
          // Allow emulator in dev mode
          if (__DEV__) return;
          break;
        case 'debugger_detected':
          // Allow debugger in dev mode
          if (__DEV__) return;
          break;
        default:
          // Block on other threats
          Alert.alert(
            'Security Alert',
            `Threat detected: ${threat}`,
            [{ text: 'OK', onPress: () => BackHandler.exitApp() }]
          );
          break;
      }
    },
    blockOnThreat: false, // We handle it manually
  });

  if (!isSecure) {
    return <CustomSecurityBlockScreen status={securityStatus} />;
  }

  return <MainApp />;
}
```

## Example 6: Integration with React Navigation

```typescript
import { useDeviceSecurity } from 'react-native-device-security';

function AppNavigator() {
  const { isSecure, isLoading } = useDeviceSecurity({
    blockOnThreat: true,
  });

  if (isLoading) {
    return <SplashScreen />;
  }

  if (!isSecure) {
    return <SecurityBlockedScreen />;
  }

  return (
    <NavigationContainer>
      <MainStack />
    </NavigationContainer>
  );
}
```

## Example 7: Periodic Security Checks

```typescript
import { useDeviceSecurity } from 'react-native-device-security';

function SecureApp() {
  const { isSecure, recheck } = useDeviceSecurity({
    checkInterval: 60000, // Check every minute
    onSecurityThreat: (threat) => {
      // Handle periodic threat detection
      console.log('Periodic check detected threat:', threat);
    },
  });

  // ... rest of app
}
```

## TypeScript Support

All examples work with TypeScript. Types are exported:

```typescript
import type {
  SecurityStatus,
  SecurityThreat,
  BlockOnSecurityThreatOptions,
  UseDeviceSecurityOptions,
  UseDeviceSecurityReturn,
} from 'react-native-device-security';
```

## Error Handling

```typescript
import DeviceSecurity from 'react-native-device-security';

try {
  const status = await DeviceSecurity.getSecurityStatus();
  // Handle status
} catch (error) {
  console.error('Security check failed:', error);
  // Fallback behavior
}
```

/**
 * react-native-device-security
 *
 * Main exports for the library
 */

// Main API
export { default as DeviceSecurity, DeviceSecurity as DeviceSecurityClass } from './api';
export { default } from './api';

// Native module spec
export { default as NativeDeviceSecurity } from './NativeDeviceSecurity';

// Hooks
export { useDeviceSecurity } from './hooks';

// Components
export { SecurityBlockedScreen } from './components';

// Types
export type {
  SecurityStatus,
  SecurityThreat,
  BlockOnSecurityThreatOptions,
  UseDeviceSecurityOptions,
  UseDeviceSecurityReturn,
} from './types';

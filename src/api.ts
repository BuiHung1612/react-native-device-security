/**
 * DeviceSecurity API
 * Main singleton class for device security checks
 */

import { Platform } from 'react-native';
import NativeDeviceSecurity from './NativeDeviceSecurity';
import type {
  BlockOnSecurityThreatOptions,
  SecurityStatus,
  SecurityThreat,
  SSLSecurityStatus,
} from './types';

/**
 * Main DeviceSecurity API class
 */
class DeviceSecurity {
  /**
   * Check if device is secure (no security threats)
   */
  async isDeviceSecure(): Promise<boolean> {
    if (Platform.OS !== 'android') {
      // iOS: no-op for now, return true
      return true;
    }

    try {
      return await NativeDeviceSecurity.isDeviceSecure();
    } catch (error) {
      console.error('DeviceSecurity: Error checking device security', error);
      return false;
    }
  }

  /**
   * Get detailed security status
   */
  async getSecurityStatus(): Promise<SecurityStatus> {
    if (Platform.OS !== 'android') {
      // iOS: no-op for now, return secure status
      return {
        isSecure: true,
        threats: [],
        isRooted: false,
        hasRootBeerDetected: false,
        hasNativeRootDetected: false,
        hasDangerousBins: false,
        hasRootApps: false,
        hasSystemPropsModified: false,
        hasFrida: false,
        hasXposed: false,
        hasMagisk: false,
        isDebuggable: false,
        isEmulator: false,
        hasSSLValidationBypass: false,
        hasSSLPinningBypass: false,
        hasProxyConfiguration: false,
        hasModifiedSSLLibraries: false,
        hasCertificateTampering: false,
        hasSSLSecurityIssue: false,
      };
    }

    try {
      const statusJson = await NativeDeviceSecurity.getSecurityStatus();
      return JSON.parse(statusJson) as SecurityStatus;
    } catch (error) {
      console.error('DeviceSecurity: Error getting security status', error);
      return this.getDefaultSecurityStatus();
    }
  }

  /**
   * Get detailed SSL security status
   */
  async getSSLSecurityStatus(): Promise<SSLSecurityStatus> {
    if (Platform.OS !== 'android') {
      // iOS: no-op for now, return secure status
      return {
        hasSSLValidationBypass: false,
        hasSSLPinningBypass: false,
        hasProxyConfiguration: false,
        hasModifiedSSLLibraries: false,
        hasCertificateTampering: false,
        hasSSLSecurityIssue: false,
      };
    }

    try {
      const statusJson = await NativeDeviceSecurity.getSSLSecurityStatus();
      return JSON.parse(statusJson) as SSLSecurityStatus;
    } catch (error) {
      console.error('DeviceSecurity: Error getting SSL security status', error);
      return {
        hasSSLValidationBypass: false,
        hasSSLPinningBypass: false,
        hasProxyConfiguration: false,
        hasModifiedSSLLibraries: false,
        hasCertificateTampering: false,
        hasSSLSecurityIssue: false,
      };
    }
  }

  /**
   * Check if device is rooted (synchronous, Android only)
   */
  isRooted(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.isRooted();
    } catch (error) {
      console.error('DeviceSecurity: Error checking root status', error);
      return false;
    }
  }

  /**
   * Check if Frida is present
   */
  hasFrida(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasFrida();
    } catch (error) {
      console.error('DeviceSecurity: Error checking Frida', error);
      return false;
    }
  }

  /**
   * Check if Xposed framework is present
   */
  hasXposed(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasXposed();
    } catch (error) {
      console.error('DeviceSecurity: Error checking Xposed', error);
      return false;
    }
  }

  /**
   * Check if Magisk is present
   */
  hasMagisk(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasMagisk();
    } catch (error) {
      console.error('DeviceSecurity: Error checking Magisk', error);
      return false;
    }
  }

  /**
   * Check if app is debuggable
   */
  isDebuggable(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.isDebuggable();
    } catch (error) {
      console.error('DeviceSecurity: Error checking debuggable', error);
      return false;
    }
  }

  /**
   * Check if running on emulator
   */
  isEmulator(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.isEmulator();
    } catch (error) {
      console.error('DeviceSecurity: Error checking emulator', error);
      return false;
    }
  }

  // ===== SSL Security Methods =====

  /**
   * Check if SSL validation has been bypassed
   */
  hasSSLValidationBypass(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasSSLValidationBypass();
    } catch (error) {
      console.error('DeviceSecurity: Error checking SSL validation bypass', error);
      return false;
    }
  }

  /**
   * Check if SSL pinning bypass tools are present
   */
  hasSSLPinningBypass(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasSSLPinningBypass();
    } catch (error) {
      console.error('DeviceSecurity: Error checking SSL pinning bypass', error);
      return false;
    }
  }

  /**
   * Check if proxy is configured (potential MITM)
   */
  hasProxyConfiguration(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasProxyConfiguration();
    } catch (error) {
      console.error('DeviceSecurity: Error checking proxy configuration', error);
      return false;
    }
  }

  /**
   * Check if SSL libraries have been modified
   */
  hasModifiedSSLLibraries(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasModifiedSSLLibraries();
    } catch (error) {
      console.error('DeviceSecurity: Error checking modified SSL libraries', error);
      return false;
    }
  }

  /**
   * Check if certificates have been tampered with
   */
  hasCertificateTampering(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasCertificateTampering();
    } catch (error) {
      console.error('DeviceSecurity: Error checking certificate tampering', error);
      return false;
    }
  }

  /**
   * Comprehensive SSL security check
   */
  hasSSLSecurityIssue(): boolean {
    if (Platform.OS !== 'android') {
      return false;
    }

    try {
      return NativeDeviceSecurity.hasSSLSecurityIssue();
    } catch (error) {
      console.error('DeviceSecurity: Error checking SSL security', error);
      return false;
    }
  }

  /**
   * Block app when security threat detected
   * This will show an alert and potentially exit the app
   */
  blockOnSecurityThreat(options: BlockOnSecurityThreatOptions = {}): void {
    if (Platform.OS !== 'android') {
      return;
    }

    const {
      showAlert = true,
      alertTitle = 'Security Warning',
      alertMessage = 'This device is not secure. The app cannot run on rooted or modified devices.',
      alertButtonText = 'OK',
      onBlocked,
    } = options;

    try {
      NativeDeviceSecurity.blockOnSecurityThreat(
        showAlert,
        alertTitle,
        alertMessage,
        alertButtonText,
      );

      // Call callback if provided
      if (onBlocked) {
        // Get status for callback
        this.getSecurityStatus().then(status => {
          onBlocked(status);
        });
      }
    } catch (error) {
      console.error('DeviceSecurity: Error blocking on security threat', error);
    }
  }

  /**
   * Get default security status (error case)
   */
  private getDefaultSecurityStatus(): SecurityStatus {
    return {
      isSecure: false,
      threats: ['emulator_detected'], // Mark as threat on error to be safe
      isRooted: false,
      hasRootBeerDetected: false,
      hasNativeRootDetected: false,
      hasDangerousBins: false,
      hasRootApps: false,
      hasSystemPropsModified: false,
      hasFrida: false,
      hasXposed: false,
      hasMagisk: false,
      isDebuggable: false,
      isEmulator: false,
      hasSSLValidationBypass: false,
      hasSSLPinningBypass: false,
      hasProxyConfiguration: false,
      hasModifiedSSLLibraries: false,
      hasCertificateTampering: false,
      hasSSLSecurityIssue: false,
    };
  }
}

// Export singleton instance
const deviceSecurityInstance = new DeviceSecurity();
export default deviceSecurityInstance;
export { DeviceSecurity };

import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  // Root detection methods
  isRooted(): boolean;
  isRootedWithDetails(): Promise<string>;

  // Hook detection methods
  hasFrida(): boolean;
  hasXposed(): boolean;
  hasMagisk(): boolean;

  // Debug detection
  isDebuggable(): boolean;

  // Emulator detection
  isEmulator(): boolean;

  // SSL security methods
  hasSSLValidationBypass(): boolean;
  hasSSLPinningBypass(): boolean;
  hasProxyConfiguration(): boolean;
  hasModifiedSSLLibraries(): boolean;
  hasCertificateTampering(): boolean;
  hasSSLSecurityIssue(): boolean;
  getSSLSecurityStatus(): Promise<string>;

  // Comprehensive security check
  getSecurityStatus(): Promise<string>;
  isDeviceSecure(): Promise<boolean>;

  // Blocking methods
  blockOnSecurityThreat(
    showAlert: boolean,
    alertTitle: string,
    alertMessage: string,
    alertButtonText: string,
  ): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('DeviceSecurity');

/**
 * Security threat types
 */
export type SecurityThreat =
  | 'root_detected'
  | 'root_beer_detected'
  | 'native_root_detected'
  | 'dangerous_bins_detected'
  | 'root_apps_detected'
  | 'system_props_modified'
  | 'frida_detected'
  | 'xposed_detected'
  | 'magisk_detected'
  | 'debugger_detected'
  | 'emulator_detected';

/**
 * Detailed security status
 */
export interface SecurityStatus {
  /** Overall security status */
  isSecure: boolean;
  /** List of detected threats */
  threats: SecurityThreat[];
  /** Device is rooted */
  isRooted: boolean;
  /** RootBeer library detected root */
  hasRootBeerDetected: boolean;
  /** Native C++ detection found root */
  hasNativeRootDetected: boolean;
  /** Dangerous binaries found */
  hasDangerousBins: boolean;
  /** Root management apps found */
  hasRootApps: boolean;
  /** System properties modified */
  hasSystemPropsModified: boolean;
  /** Frida framework detected */
  hasFrida: boolean;
  /** Xposed framework detected */
  hasXposed: boolean;
  /** Magisk detected */
  hasMagisk: boolean;
  /** Debugger is attached */
  isDebuggable: boolean;
  /** Running on emulator */
  isEmulator: boolean;
  /** Additional details about detection */
  details?: Record<string, boolean | string | number>;
}

/**
 * Options for blocking on security threat
 */
export interface BlockOnSecurityThreatOptions {
  /** Show alert to user when blocked */
  showAlert?: boolean;
  /** Custom alert title (default: 'Security Warning') */
  alertTitle?: string;
  /** Custom alert message */
  alertMessage?: string;
  /** Alert button text (default: 'OK') */
  alertButtonText?: string;
  /** Callback when app is blocked */
  onBlocked?: (status: SecurityStatus) => void;
}

/**
 * Hook options
 */
export interface UseDeviceSecurityOptions {
  /** Callback when security threat detected */
  onSecurityThreat?: (threat: SecurityThreat, status: SecurityStatus) => void;
  /** Block app when threat detected */
  blockOnThreat?: boolean;
  /** Custom block options */
  blockOptions?: BlockOnSecurityThreatOptions;
  /** Check interval in ms (default: 0 - check once) */
  checkInterval?: number;
}

/**
 * Hook return value
 */
export interface UseDeviceSecurityReturn {
  /** Device is secure */
  isSecure: boolean | null;
  /** Loading state */
  isLoading: boolean;
  /** Security status details */
  securityStatus: SecurityStatus | null;
  /** Error if any */
  error: Error | null;
  /** Manually re-check security */
  recheck: () => Promise<void>;
}

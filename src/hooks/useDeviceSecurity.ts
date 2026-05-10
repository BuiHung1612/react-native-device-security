/**
 * React hook for device security
 */

import { useCallback, useEffect, useRef, useState } from 'react';
import { Platform } from 'react-native';
import deviceSecurity from '../api';
import type {
  SecurityStatus,
  UseDeviceSecurityOptions,
  UseDeviceSecurityReturn,
} from '../types';

export function useDeviceSecurity(
  options: UseDeviceSecurityOptions = {},
): UseDeviceSecurityReturn {
  const { checkInterval = 0 } = options;

  const [isSecure, setIsSecure] = useState<boolean | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [securityStatus, setSecurityStatus] = useState<SecurityStatus | null>(
    null,
  );
  const [error, setError] = useState<Error | null>(null);

  // Use refs for options to avoid infinite loops when inline objects/functions are passed
  const optionsRef = useRef(options);
  useEffect(() => {
    optionsRef.current = options;
  }, [options]);

  const checkSecurity = useCallback(async () => {
    if (Platform.OS !== 'android') {
      setIsSecure(true);
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      setError(null);

      const status = await deviceSecurity.getSecurityStatus();
      setSecurityStatus(status);
      setIsSecure(status.isSecure);

      // Handle security threats
      if (!status.isSecure) {
        const currentOptions = optionsRef.current;
        
        // Call callback for each threat
        for (const threat of status.threats) {
          currentOptions.onSecurityThreat?.(threat, status);
        }

        // Block if requested
        if (currentOptions.blockOnThreat) {
          deviceSecurity.blockOnSecurityThreat(currentOptions.blockOptions);
        }
      }
    } catch (err) {
      const errorObj =
        err instanceof Error ? err : new Error('Security check failed');
      setError(errorObj);
      setIsSecure(false);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const recheck = useCallback(async () => {
    await checkSecurity();
  }, [checkSecurity]);

  useEffect(() => {
    checkSecurity();

    // Set up interval if specified
    if (checkInterval > 0) {
      const intervalId = setInterval(checkSecurity, checkInterval);
      return () => clearInterval(intervalId);
    }
  }, [checkSecurity, checkInterval]);

  return {
    isSecure,
    isLoading,
    securityStatus,
    error,
    recheck,
  };
}

export default useDeviceSecurity;

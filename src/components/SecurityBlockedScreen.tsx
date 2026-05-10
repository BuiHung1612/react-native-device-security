/**
 * SecurityBlockedScreen.tsx
 *
 * Screen to display when app is blocked due to security threats
 */

import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  SafeAreaView,
  Platform,
} from 'react-native';
import type { SecurityStatus, SecurityThreat } from '../types';

interface SecurityBlockedScreenProps {
  /** Security status details */
  threats?: SecurityThreat[];
  /** Custom title */
  title?: string;
  /** Custom message */
  message?: string;
  /** Custom icon component */
  icon?: React.ReactNode;
}

const THREAT_TITLES: Record<SecurityThreat, string> = {
  root_detected: 'Thiết bị đã root',
  root_beer_detected: 'Phát hiện root (RootBeer)',
  native_root_detected: 'Phát hiện root (Native)',
  dangerous_bins_detected: 'Phát hiện binary nguy hiểm',
  root_apps_detected: 'Phát hiện ứng dụng root',
  system_props_modified: 'Thuộc tính hệ thống đã sửa đổi',
  frida_detected: 'Phát hiện Frida (Hooking)',
  xposed_detected: 'Phát hiện Xposed Framework',
  magisk_detected: 'Phát hiện Magisk',
  debugger_detected: 'Phát hiện Debugger',
  emulator_detected: 'Phát hiện Bộ giả lập',
};

const THREAT_DESCRIPTIONS: Record<SecurityThreat, string> = {
  root_detected: 'Thiết bị của bạn đã được root, làm giảm tính bảo mật của ứng dụng.',
  root_beer_detected: 'Công cụ phát hiện root đã phát hiện thiết bị đã bị can thiệp.',
  native_root_detected: 'Phát hiện native root - thiết bị có thể đã bị sửa đổi.',
  dangerous_bins_detected: 'Phát hiện các file thực thi nguy hiểm trên thiết bị.',
  root_apps_detected: 'Phát hiện ứng dụng quản lý root trên thiết bị.',
  system_props_modified: 'Các thuộc tính hệ thống quan trọng đã bị sửa đổi.',
  frida_detected: 'Phát hiện công cụ Frida - thường dùng để can thiệp vào ứng dụng.',
  xposed_detected: 'Phát hiện Xposed Framework - có thể can thiệp vào ứng dụng.',
  magisk_detected: 'Phát hiện Magisk - công cụ root ẩn danh.',
  debugger_detected: 'Phát hiện debugger đang gắn vào ứng dụng.',
  emulator_detected: 'Ứng dụng đang chạy trên bộ giả lập, không an toàn cho môi trường production.',
};

/**
 * Default security blocked screen component
 */
export const SecurityBlockedScreen: React.FC<SecurityBlockedScreenProps> = ({
  threats = [],
  title = 'Cảnh báo bảo mật',
  message = 'Thiết bị của bạn không đáp ứng yêu cầu bảo mật. Ứng dụng không thể tiếp tục chạy.',
  icon,
}) => {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        bounces={false}
      >
        {icon || (
          <View style={styles.iconContainer}>
            <Text style={styles.icon}>🔒</Text>
          </View>
        )}

        <Text style={styles.title}>{title}</Text>

        <Text style={styles.message}>{message}</Text>

        {threats.length > 0 && (
          <>
            <Text style={styles.threatsTitle}>Vấn đề phát hiện:</Text>

            <View style={styles.threatsList}>
              {threats.map((threat) => (
                <View key={threat} style={styles.threatItem}>
                  <View style={styles.bullet} />
                  <View style={styles.threatContent}>
                    <Text style={styles.threatTitle}>
                      {THREAT_TITLES[threat] || threat}
                    </Text>
                    <Text style={styles.threatDescription}>
                      {THREAT_DESCRIPTIONS[threat] || 'Vấn đề bảo mật không xác định.'}
                    </Text>
                  </View>
                </View>
              ))}
            </View>
          </>
        )}

        <View style={styles.footer}>
          <Text style={styles.footerText}>
            Nếu bạn cho rằng đây là sự nhầm lẫn, vui lòng liên hệ với bộ phận hỗ trợ.
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollContent: {
    padding: 24,
    alignItems: 'center',
  },
  iconContainer: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: '#ffebee',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 24,
  },
  icon: {
    fontSize: 48,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#d32f2f',
    textAlign: 'center',
    marginBottom: 12,
  },
  message: {
    fontSize: 16,
    color: '#424242',
    textAlign: 'center',
    marginBottom: 24,
    lineHeight: 24,
  },
  threatsTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#212121',
    alignSelf: 'flex-start',
    marginBottom: 16,
  },
  threatsList: {
    width: '100%',
    marginBottom: 24,
  },
  threatItem: {
    flexDirection: 'row',
    backgroundColor: '#ffffff',
    borderRadius: 8,
    padding: 16,
    marginBottom: 12,
    borderLeftWidth: 4,
    borderLeftColor: '#d32f2f',
  },
  bullet: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#d32f2f',
    marginTop: 6,
    marginRight: 12,
  },
  threatContent: {
    flex: 1,
  },
  threatTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#212121',
    marginBottom: 4,
  },
  threatDescription: {
    fontSize: 14,
    color: '#757575',
    lineHeight: 20,
  },
  footer: {
    marginTop: 24,
    paddingTop: 24,
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
  },
  footerText: {
    fontSize: 14,
    color: '#9e9e9e',
    textAlign: 'center',
  },
});

export default SecurityBlockedScreen;

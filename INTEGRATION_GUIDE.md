# Integration Guide - react-native-device-security

## Cách tích hợp vào dự án React Native hiện có

### Cách 1: Tích hợp trực tiếp (Copy files)

1. **Copy thư viện vào project:**

```bash
# Copy thư viện vào thư mục gốc của project
cp -r react-native-device-security /path/to/your/project/
```

2. **Cấu hình Android:**

Thêm vào `android/settings.gradle`:

```gradle
include ':react-native-device-security'
project(':react-native-device-security').projectDir = new File(rootProject.projectDir, '../react-native-device-security/android')
```

Thêm vào `android/app/build.gradle`:

```gradle
dependencies {
    implementation project(':react-native-device-security')
}
```

Thêm vào `android/app/proguard-rules.pro` (nếu có):

```proguard
-keep class vn.osp.security.** { *; }
-keepclassmembers class vn.osp.security.** { *; }
-dontwarn vn.osp.security.**
```

3. **Đăng ký module trong MainApplication:**

```kotlin
// android/app/src/main/java/com/yourapp/MainApplication.kt
package com.yourapp

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint
import com.facebook.react.defaults.DefaultReactNativeHost
import vn.osp.security.DeviceSecurityPackage  // <--- Thêm dòng này

class MainApplication : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): MutableList<ReactPackage> =
                PackageList.getPackages(this).apply {
                    // Packages được thêm ở đây
                    add(DeviceSecurityPackage())  // <--- Thêm dòng này
                }

            // ... code khác
        }
}
```

Hoặc nếu dùng Java:

```java
// android/app/src/main/java/com/yourapp/MainApplication.java
package com.yourapp;

import android.app.Application;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactNativeHost;
import vn.osp.security.DeviceSecurityPackage;  // <--- Thêm dòng này

public class MainApplication extends Application implements ReactApplication {

    @Override
    public ReactNativeHost getReactNativeHost() {
        return new DefaultReactNativeHost(this) {
            @Override
            protected List<ReactPackage> getPackages() {
                @SuppressWarnings("UnnecessaryLocalVariable")
                List<ReactPackage> packages = new PackageList(this).getPackages();
                // Packages được thêm ở đây
                packages.add(new DeviceSecurityPackage());  // <--- Thêm dòng này
                return packages;
            }

            // ... code khác
        };
    }
}
```

4. **Sử dụng trong React Native:**

```typescript
// src/App.tsx
import React, { useEffect } from 'react';
import { View, Text, Alert } from 'react-native';
import DeviceSecurity from 'react-native-device-security'; // hoặc đường dẫn tương đối

function App() {
  useEffect(() => {
    // Kiểm tra bảo mật khi app khởi động
    DeviceSecurity.blockOnSecurityThreat({
      showAlert: true,
      alertTitle: 'Cảnh báo bảo mật',
      alertMessage: 'Thiết bị của bạn không an toàn. Ứng dụng không thể chạy.',
      onBlocked: (status) => {
        console.log('App blocked due to:', status.threats);
      },
    });
  }, []);

  return (
    <View>
      <Text>My App</Text>
    </View>
  );
}

export default App;
```

### Cách 2: Sử dụng với hook (Khuyến nghị)

```typescript
// src/App.tsx
import React from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useDeviceSecurity, SecurityBlockedScreen } from 'react-native-device-security';

function App() {
  const { isSecure, isLoading, securityStatus } = useDeviceSecurity({
    blockOnThreat: true, // Tự động chặn khi phát hiện
    blockOptions: {
      showAlert: true,
      alertTitle: 'Cảnh báo bảo mật',
      alertMessage: 'Thiết bị không an toàn',
    },
  });

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" />
        <Text>Đang kiểm tra bảo mật...</Text>
      </View>
    );
  }

  if (!isSecure) {
    return <SecurityBlockedScreen threats={securityStatus?.threats} />;
  }

  return (
    <View>
      <Text>My App - An toàn</Text>
    </View>
  );
}

export default App;
```

### Cách 3: Import từ đường dẫn tương đối

Nếu không muốn cài đặt package:

```typescript
// Import trực tiếp từ thư mục
import DeviceSecurity from '../react-native-device-security/src';
// hoặc
import { useDeviceSecurity } from '../react-native-device-security/src/hooks';
```

## Cách publish lên npm (tương lai)

1. **Publish lên npm:**

```bash
cd react-native-device-security
npm publish
```

2. **Sau đó có thể cài đặt bình thường:**

```bash
npm install react-native-device-security
# hoặc
yarn add react-native-device-security
```

## Testing

Để test library, bạn có thể:

1. **Test trên thiết bị thật đã root:** Cài app lên thiết bị root để xem có bị chặn không
2. **Test trên emulator:** App nên bị chặn khi chạy trên emulator
3. **Test với Frida:** Chạy Frida để xem có phát hiện không
4. **Dev mode:** Trong dev mode, có thể tạm thời tắt block để test

## Troubleshooting

### Lỗi "Native module not found"

1. Kiểm tra đã add dependency vào `android/app/build.gradle` chưa
2. Kiểm tra đã register package trong `MainApplication` chưa
3. Clean và rebuild:

```bash
cd android
./gradlew clean
cd ..
npx react-native run-android
```

### Lỗi build native

1. Kiểm tra NDK đã cài đặt chưa
2. Kiểm tra `android/local.properties` có path đến NDK chưa
3. Cập nhật NDK version trong `android/build.gradle`

### App bị chặn trên thiết bị an toàn

1. Kiểm tra xem có đang chạy trên emulator không
2. Kiểm tra logcat để xem lý do cụ thể
3. Có thể tạm thời disable trong dev mode:

```typescript
// Chỉ block trong release build
if (!__DEV__) {
  DeviceSecurity.blockOnSecurityThreat();
}
```

## Gradle sync trong Android Studio

1. Mở Android Studio
2. File → Sync Project with Gradle Files
3. Build → Rebuild Project

## Xóa thư viện (nếu cần)

1. Xóa dependency khỏi `android/app/build.gradle`
2. Xóa import khỏi `MainApplication`
3. Xóa thư mục `react-native-device-security`
4. Clean build:

```bash
cd android
./gradlew clean
```

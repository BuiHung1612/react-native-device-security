# Hướng dẫn chuyển sang Standalone Repo

## Cách chuyển thư viện sang repo riêng

### Cách 1: Copy thủ công

1. **Tạo repo mới trên GitHub:**
   - Tên: `react-native-device-security`
   - Description: Multi-layer device security detection for React Native
   - License: MIT

2. **Clone repo về:**
   ```bash
   git clone https://github.com/your-org/react-native-device-security.git
   cd react-native-device-security
   ```

3. **Copy nội dung thư mục:**
   ```bash
   # Copy từ thư mục hiện tại sang repo mới
   cp -r /path/to/react-native-device-security/* .
   ```

4. **Kiểm tra và commit:**
   ```bash
   git add .
   git commit -m "Initial commit: Add react-native-device-security library"
   git push origin main
   ```

### Cách 2: Dùng git subtree (khuyên dùng)

```bash
# Từ repo hiện tại, export thư viện sang repo riêng
cd /path/to/your/project
git subtree split --prefix=react-native-device-security -b device-security-branch

# Push sang repo mới
git push https://github.com/your-org/react-native-device-security.git device-security-branch:main
```

### Cách 3: Dùng git filter-repo (nhanh nhất)

```bash
# Cài git-filter-repo
pip install git-filter-repo

# Từ thư mục project hiện tại
cd /path/to/your/project
git filter-repo --path react-native-device-security/ --to-subdirectory-filter / --refs main

# Clone sang repo mới
git clone https://github.com/your-org/react-native-device-security.git temp-repo
cd temp-repo
git pull /path/to/your/project main
git push origin main
```

## Cập nhật file sau khi chuyển repo

### 1. Cập nhật package.json

```json
{
  "repository": {
    "type": "git",
    "url": "https://github.com/your-org/react-native-device-security.git"
  },
  "bugs": {
    "url": "https://github.com/your-org/react-native-device-security/issues"
  },
  "homepage": "https://github.com/your-org/react-native-device-security#readme"
}
```

### 2. Cập nhật README.md

Thay đổi các link GitHub về repo mới.

### 3. Tạo LICENSE file

Đã có file LICENSE, giữ nguyên.

## Publish lên npm

### 1. Tạo npm account (nếu chưa có)

```bash
npm adduser
```

### 2. Publish

```bash
npm publish
```

### 3. Publish scoped package

Nếu muốn publish với scope:

```bash
# Tạo scope
npm init --scope=@your-org

# Publish
npm publish --access public
```

## Cấu trúc repo cuối cùng

```
react-native-device-security/
├── src/                          # TypeScript source
│   ├── index.ts                  # Main API
│   ├── types.ts                  # Type definitions
│   ├── NativeDeviceSecurity.ts   # Native spec
│   ├── components/               # UI components
│   │   ├── SecurityBlockedScreen.tsx
│   │   └── index.ts
│   └── hooks/                    # React hooks
│       ├── useDeviceSecurity.ts
│       └── index.ts
├── android/                      # Android native code
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/vn/osp/security/
│       │   ├── DeviceSecurityModule.kt
│       │   ├── DeviceSecurityPackage.kt
│       │   ├── RootDetection.kt
│       │   ├── HookDetection.kt
│       │   ├── DebugDetection.kt
│       │   ├── EmulatorDetection.kt
│       │   └── NativeSecurityCheck.kt
│       └── cpp/                  # Native C++ code
│           ├── CMakeLists.txt
│           └── device-security.cpp
├── ios/                          # iOS native code (placeholder)
│   └── (tương lai)
├── package.json                  # NPM config
├── tsconfig.json                 # TypeScript config
├── react-native-device-security.podspec  # CocoaPods spec
├── README.md                     # Documentation
├── LICENSE                       # MIT License
├── INTEGRATION_GUIDE.md          # Integration guide
└── .gitignore
```

## Workflow phát triển

### 1. Development mode

```bash
# Trong thư viện
npm run watch

# Trong project sử dụng
npm link ../react-native-device-security
```

### 2. Testing

```bash
# Test với example app
cd example
npm install
npm run android
```

### 3. Release

```bash
# Bump version
npm version patch  # hoặc minor, major

# Publish
npm publish

# Git tag
git push origin main --tags
```

## Maintenance

### Update dependencies

```bash
npm update
cd android
./gradlew dependencies
```

### Update README

Cập nhật version số và changelog khi release mới.

### Changelog format

```markdown
## [1.0.0] - 2025-01-XX

### Added
- Multi-layer root detection
- Frida/Xposed/Magisk detection
- Native C++ detection
- React hook integration
- Security blocked UI component

### Changed
- N/A

### Fixed
- N/A

### Security
- Initial release
```

## Testing sau khi publish

```bash
# Test installation
npm install react-native-device-security

# Test import
import DeviceSecurity from 'react-native-device-security';
console.log(DeviceSecurity);
```

## Continuous Integration (tùy chọn)

### GitHub Actions

```yaml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 18
      - run: npm install
      - run: npm test
      - run: npm run lint
```

## Tips

1. **Semantic Versioning**: Theo semver.org
2. **Breaking Changes**: Bump major version
3. **Documentation**: Cập nhật README cho mỗi feature mới
4. **Examples**: Cung cấp example app cho người dùng
5. **Issues**: Respond nhanh cho community

## Kết luận

Sau khi chuyển sang standalone repo:
- Library có thể tái sử dụng ở nhiều project
- Dễ maintain và update
- Có thể share cho community
- Tách biệt với business logic của project hiện tại

# Quick Start: Standalone Repository

## Method 1: Automated Script (Recommended)

### macOS/Linux

```bash
cd /path/to/pho-bien-gdpl
chmod +x react-native-device-security/scripts/setup-standalone.sh
./react-native-device-security/scripts/setup-standalone.sh
```

### Windows

```powershell
cd \path\to\pho-bien-gdpl
.\react-native-device-security\scripts\setup-standalone.ps1
```

## Method 2: Manual Setup

### Step 1: Copy to new location

```bash
# Create new directory
mkdir ~/react-native-device-security-standalone
cd ~/react-native-device-security-standalone

# Copy files
cp -r /path/to/pho-bien-gdpl/react-native-device-security/* .
```

### Step 2: Initialize git

```bash
git init
git add .
git commit -m "Initial commit"
```

### Step 3: Create GitHub repository

1. Go to https://github.com/new
2. Create new repository:
   - **Name**: `react-native-device-security`
   - **Description**: Multi-layer device security detection for React Native
   - **License**: MIT
   - **Important**: Don't initialize with README

### Step 4: Push to GitHub

```bash
# Add remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/react-native-device-security.git
git branch -M main
git push -u origin main
```

### Step 5: Update repository URLs

Edit `package.json` and `README.md` to replace:
- `https://github.com/your-org/react-native-device-security.git`
- With your actual repository URL

```bash
# Commit updates
git add package.json README.md
git commit -m "chore: update repository URLs"
git push
```

## Method 3: Git Subtree (Advanced)

If the parent project is already a git repo:

```bash
cd /path/to/pho-bien-gdpl

# Create a branch with only the library
git subtree split --prefix=react-native-device-security -b device-security-branch

# Push to new repository
git push https://github.com/YOUR_USERNAME/react-native-device-security.git device-security-branch:main
```

## After Setup

### Option A: Publish to npm

```bash
cd ~/react-native-device-security-standalone
npm publish
```

### Option B: Install from local

```bash
# In your project
npm install /path/to/react-native-device-security-standalone
```

### Option C: Install from git

```bash
# In your project
npm install https://github.com/YOUR_USERNAME/react-native-device-security.git
```

## Verify Installation

```bash
# In your project
npm list react-native-device-security
```

## Test the Library

```typescript
import DeviceSecurity from 'react-native-device-security';

const isSecure = await DeviceSecurity.isDeviceSecure();
console.log('Device is secure:', isSecure);
```

## Troubleshooting

### Script not executable (macOS/Linux)

```bash
chmod +x react-native-device-security/scripts/setup-standalone.sh
```

### PowerShell execution policy (Windows)

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Git remote already exists

```bash
git remote remove origin
git remote add origin https://github.com/YOUR_USERNAME/react-native-device-security.git
```

### Permission denied when pushing

1. Check GitHub authentication: https://github.com/settings/tokens
2. Create personal access token with `repo` scope
3. Use token as password:
   ```bash
   git push https://YOUR_TOKEN@github.com/YOUR_USERNAME/react-native-device-security.git
   ```

## Next Steps

1. ✅ Setup standalone repository
2. ✅ Push to GitHub
3. ✅ Publish to npm (optional)
4. ✅ Install in your project
5. ✅ Test functionality
6. ✅ Integrate into main project

See [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) for integration steps.

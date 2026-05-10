# PowerShell script to setup react-native-device-security as a standalone repository
# Usage: .\setup-standalone.ps1

Write-Host "🚀 Setting up react-native-device-security as standalone repo..." -ForegroundColor Green

$CurrentDir = Get-Location
$LibDir = Join-Path $CurrentDir "react-native-device-security"

# Check if library directory exists
if (-not (Test-Path $LibDir)) {
    Write-Host "❌ Error: react-native-device-security directory not found!" -ForegroundColor Red
    Write-Host "Please run this script from the project root."
    exit 1
}

Write-Host "✓ Found library at: $LibDir" -ForegroundColor Green

# Menu
Write-Host ""
Write-Host "Choose setup method:" -ForegroundColor Yellow
Write-Host "1) Copy to new directory (recommended for testing)"
Write-Host "2) Initialize git in current location"
$choice = Read-Host "Enter choice (1-2)"

switch ($choice) {
    "1" {
        $destPath = Read-Host "Enter destination path"
        Write-Host "📦 Copying to $destPath..."
        New-Item -ItemType Directory -Force -Path $destPath | Out-Null
        Copy-Item -Path "$LibDir\*" -Destination $destPath -Recurse -Force
        Set-Location $destPath
    }
    "2" {
        Write-Host "📦 Setting up git in current location..."
        Set-Location $LibDir
    }
    default {
        Write-Host "❌ Invalid choice" -ForegroundColor Red
        exit 1
    }
}

# Initialize git repo
Write-Host ""
Write-Host "🔧 Initializing git repository..."
git init

# Create .gitignore if it doesn't exist
$gitignorePath = ".gitignore"
if (-not (Test-Path $gitignorePath)) {
    @"
# Dependencies
node_modules/
npm-debug.log
yarn-error.log
yarn-debug.log

# OS
.DS_Store
Thumbs.db

# IDE
.idea/
.vscode/
*.swp
*.swo
*~

# Build
lib/
dist/
build/
*.log

# Android
android/build/
android/.gradle/
android/local.properties
android/*.iml

# iOS
ios/Pods/
ios/build/

# Temp
.tmp/
temp/
"@ | Out-File -FilePath $gitignorePath -Encoding utf8
    Write-Host "✓ Created .gitignore" -ForegroundColor Green
}

# Create initial commit
Write-Host ""
Write-Host "📝 Creating initial commit..."
git add .
git commit -m "Initial commit: Add react-native-device-security library

Features:
- Multi-layer root detection (RootBeer + Native C++)
- Frida/Xposed/Magisk detection
- Debugger detection
- Emulator detection
- React hook integration
- Security blocked UI component"

# Ask for GitHub repo setup
Write-Host ""
Write-Host "🌐 GitHub Repository Setup" -ForegroundColor Yellow
$setupGithub = Read-Host "Do you want to setup GitHub repository? (y/n)"

if ($setupGithub -eq "y") {
    $githubUser = Read-Host "Enter your GitHub username"
    $repoName = Read-Host "Enter repository name"

    $repoUrl = "https://github.com/$githubUser/$repoName.git"

    Write-Host ""
    Write-Host "📋 Instructions:"
    Write-Host "1. Create a new repository on GitHub: https://github.com/new"
    Write-Host "   - Name: $repoName"
    Write-Host "   - Description: Multi-layer device security detection for React Native"
    Write-Host "   - License: MIT"
    Write-Host "   - Don't initialize with README (we already have one)"
    Write-Host ""
    Write-Host "2. After creating, press Enter to continue..."
    Read-Host

    # Add remote
    git remote add origin $repoUrl
    git branch -M main
    git push -u origin main

    Write-Host "✓ Pushed to GitHub: $repoUrl" -ForegroundColor Green

    # Update package.json and README.md
    $packageJson = Get-Content "package.json" -Raw
    $packageJson = $packageJson -replace 'https://github.com/your-org/react-native-device-security.git', $repoUrl
    $packageJson = $packageJson -replace 'https://github.com/your-org/react-native-device-security', "https://github.com/$githubUser/$repoName"
    $packageJson | Out-File "package.json" -Encoding utf8

    $readme = Get-Content "README.md" -Raw
    $readme = $readme -replace 'https://github.com/your-org/react-native-device-security', "https://github.com/$githubUser/$repoName"
    $readme | Out-File "README.md" -Encoding utf8

    git add package.json README.md
    git commit -m "chore: update repository URLs"
    git push

    Write-Host "✓ Updated repository URLs" -ForegroundColor Green
}

Write-Host ""
Write-Host "✅ Setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:"
Write-Host "1. Publish to npm: npm publish"
Write-Host "2. Or install locally: npm install /path/to/repo"
Write-Host "3. Or install from git: npm install github.com/$githubUser/$repoName"

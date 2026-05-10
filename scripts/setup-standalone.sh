#!/bin/bash

# Script to setup react-native-device-security as a standalone repository
# Usage: ./setup-standalone.sh

set -e

echo "🚀 Setting up react-native-device-security as standalone repo..."

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Current directory
CURRENT_DIR=$(pwd)
LIB_DIR="$CURRENT_DIR/react-native-device-security"

# Check if library directory exists
if [ ! -d "$LIB_DIR" ]; then
    echo "❌ Error: react-native-device-security directory not found!"
    echo "Please run this script from the project root."
    exit 1
fi

echo -e "${GREEN}✓${NC} Found library at: $LIB_DIR"

# Option 1: Copy to new location
echo ""
echo "Choose setup method:"
echo "1) Copy to new directory (recommended for testing)"
echo "2) Initialize git in current location"
echo "3) Export using git (if parent is a git repo)"
read -p "Enter choice (1-3): " choice

case $choice in
  1)
    read -p "Enter destination path: " DEST_PATH
    echo "📦 Copying to $DEST_PATH..."
    mkdir -p "$DEST_PATH"
    cp -r "$LIB_DIR"/* "$DEST_PATH/"
    cd "$DEST_PATH"
    ;;
  2)
    echo "📦 Setting up git in current location..."
    cd "$LIB_DIR"
    ;;
  3)
    echo "📦 Exporting from git..."
    cd "$CURRENT_DIR"

    # Create a temporary directory for the exported code
    TEMP_DIR=$(mktemp -d)
    echo "Using temp directory: $TEMP_DIR"

    # Copy all files from the library directory
    cp -r "$LIB_DIR"/* "$TEMP_DIR/"

    # Change to the temp directory
    cd "$TEMP_DIR"
    ;;
  *)
    echo "❌ Invalid choice"
    exit 1
    ;;
esac

# Initialize git repo
echo ""
echo "🔧 Initializing git repository..."
git init

# Create .gitignore if it doesn't exist
if [ ! -f .gitignore ]; then
    cat > .gitignore << 'EOF'
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
EOF
    echo -e "${GREEN}✓${NC} Created .gitignore"
fi

# Create initial commit
echo ""
echo "📝 Creating initial commit..."
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
echo ""
echo "🌐 GitHub Repository Setup"
read -p "Do you want to setup GitHub repository? (y/n): " setup_github

if [ "$setup_github" = "y" ]; then
    read -p "Enter your GitHub username: " github_user
    read -p "Enter repository name: " repo_name

    REPO_URL="https://github.com/$github_user/$repo_name.git"

    echo ""
    echo "📋 Instructions:"
    echo "1. Create a new repository on GitHub: https://github.com/new"
    echo "   - Name: $repo_name"
    echo "   - Description: Multi-layer device security detection for React Native"
    echo "   - License: MIT"
    echo "   - Don't initialize with README (we already have one)"
    echo ""
    echo "2. After creating, press Enter to continue..."
    read

    # Add remote
    git remote add origin "$REPO_URL"
    git branch -M main
    git push -u origin main

    echo -e "${GREEN}✓${NC} Pushed to GitHub: $REPO_URL"
fi

# Update package.json with actual repo URL
if [ "$setup_github" = "y" ] && [ ! -z "$repo_name" ]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s|https://github.com/your-org/react-native-device-security.git|https://github.com/$github_user/$repo_name.git|g" package.json
        sed -i '' "s|https://github.com/your-org/react-native-device-security|https://github.com/$github_user/$repo_name|g" package.json
        sed -i '' "s|https://github.com/your-org/react-native-device-security.git|https://github.com/$github_user/$repo_name.git|g" README.md
    else
        # Linux
        sed -i "s|https://github.com/your-org/react-native-device-security.git|https://github.com/$github_user/$repo_name.git|g" package.json
        sed -i "s|https://github.com/your-org/react-native-device-security|https://github.com/$github_user/$repo_name|g" package.json
        sed -i "s|https://github.com/your-org/react-native-device-security.git|https://github.com/$github_user/$repo_name.git|g" README.md
    fi

    git add package.json README.md
    git commit -m "chore: update repository URLs"
    git push

    echo -e "${GREEN}✓${NC} Updated repository URLs"
fi

echo ""
echo -e "${GREEN}✅ Setup complete!${NC}"
echo ""
echo "Next steps:"
echo "1. Publish to npm: npm publish"
echo "2. Or install locally: npm install /path/to/repo"
echo "3. Or install from git: npm install github.com/$github_user/$repo_name"

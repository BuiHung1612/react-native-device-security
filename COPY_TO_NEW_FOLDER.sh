#!/bin/bash

# Quick script to copy library to a new folder for standalone setup
# Usage: ./COPY_TO_NEW_FOLDER.sh /path/to/destination

if [ -z "$1" ]; then
    echo "Usage: $0 /path/to/destination"
    echo "Example: $0 ~/react-native-device-security"
    exit 1
fi

DEST_PATH="$1"
LIB_DIR="$(dirname "$0")"

echo "📦 Copying react-native-device-security to $DEST_PATH..."

# Create destination directory
mkdir -p "$DEST_PATH"

# Copy all files
cp -r "$LIB_DIR"/* "$DEST_PATH/"

echo "✅ Files copied successfully!"
echo ""
echo "Next steps:"
echo "1. cd $DEST_PATH"
echo "2. git init"
echo "3. git add ."
echo "4. git commit -m 'Initial commit'"
echo "5. Create GitHub repo at: https://github.com/new"
echo "6. git remote add origin https://github.com/YOUR_USERNAME/react-native-device-security.git"
echo "7. git push -u origin main"

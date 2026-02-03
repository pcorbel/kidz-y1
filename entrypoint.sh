#!/bin/bash
set -e

# Configuration
PACKAGE_NAME="com.kidz.y1"
APK_SOURCE_PATH="app/build/outputs/apk/release/app-release.apk"
BASE_ROM_DIR="${BASE_ROM_DIR:-/opt/base-rom}"
WORK_DIR="./build/rom-build"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Cleanup function for ROM builds
cleanup_rom() {
    local MOUNT_SYS="$WORK_DIR/mount_sys"
    local MOUNT_USER="$WORK_DIR/mount_user"
    
    if mountpoint -q "$MOUNT_SYS" 2>/dev/null; then
        echo_info "Unmounting system image..."
        umount "$MOUNT_SYS" || true
    fi
    
    if mountpoint -q "$MOUNT_USER" 2>/dev/null; then
        echo_info "Unmounting userdata image..."
        umount "$MOUNT_USER" || true
    fi
    
    if [ -d "$WORK_DIR" ]; then
        rm -rf "$WORK_DIR"
    fi
}

# Function to convert sparse image to raw if needed
convert_sparse_to_raw() {
    local IMG_FILE="$1"
    local RAW_FILE="${IMG_FILE%.img}_raw.img"
    
    # Check if image is sparse (Android sparse image format starts with magic bytes 0x3AFF26ED)
    # file command is installed in Dockerfile
    IS_SPARSE=false
    if file "$IMG_FILE" 2>/dev/null | grep -q "Android sparse image"; then
        IS_SPARSE=true
    else
        # Fallback: check magic bytes directly (0x3AFF26ED at offset 0)
        # hexdump is standard in Ubuntu
        MAGIC=$(hexdump -n 4 -e '4/1 "%02x"' "$IMG_FILE" 2>/dev/null)
        if [ "$MAGIC" = "3aff26ed" ] || [ "$MAGIC" = "ed26ff3a" ]; then
            IS_SPARSE=true
            echo_info "Detected sparse image $(basename $IMG_FILE) via magic bytes: $MAGIC" >&2
        fi
    fi
    
    if [ "$IS_SPARSE" = true ]; then
        echo_info "Converting sparse image $(basename $IMG_FILE) to raw format..." >&2
        
        # simg2img is installed at /usr/local/bin/simg2img by Dockerfile
        SIMG2IMG="/usr/local/bin/simg2img"
        
        # Convert sparse to raw
        echo_info "Running: $SIMG2IMG $IMG_FILE $RAW_FILE" >&2
        "$SIMG2IMG" "$IMG_FILE" "$RAW_FILE" || exit 1
        echo_info "Successfully converted $(basename $IMG_FILE) to $(basename $RAW_FILE)" >&2
        echo "$RAW_FILE"
    else
        # Not a sparse image, return original file
        echo "$IMG_FILE"
    fi
}

# Build ROM zip
echo_info "=== Building ROM zip ==="

# Check prerequisites
if [ ! -d "$BASE_ROM_DIR" ] || [ ! -f "$BASE_ROM_DIR/system.img" ] || [ ! -f "$BASE_ROM_DIR/userdata.img" ]; then
    echo_error "Base ROM images not found in $BASE_ROM_DIR"
    exit 1
fi

# Check if pre-built APK exists
if [ ! -f "$APK_SOURCE_PATH" ]; then
    echo_error "Pre-built APK not found at $APK_SOURCE_PATH"
    echo_error "Please build the APK locally first: ./gradlew assembleRelease"
    exit 1
fi

echo_info "Using pre-built APK: $APK_SOURCE_PATH"

# Create build directory and copy APK
mkdir -p build
cp "$APK_SOURCE_PATH" "build/kidz.apk"

# Setup trap for cleanup
trap cleanup_rom EXIT

# Create ROM zip
echo_info "Creating ROM zip..."
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR/base" "$WORK_DIR/mount_sys" "$WORK_DIR/mount_user"

ABS_WORK_DIR=$(cd "$WORK_DIR" && pwd)
ABS_MOUNT_SYS="$ABS_WORK_DIR/mount_sys"
ABS_MOUNT_USER="$ABS_WORK_DIR/mount_user"

# Copy base ROM images and other ROM files
echo_info "Copying base ROM files..."
cp "$BASE_ROM_DIR/system.img" "$WORK_DIR/base/"
cp "$BASE_ROM_DIR/userdata.img" "$WORK_DIR/base/"

# Copy other ROM files (boot, recovery, logo, etc.) if they exist
for file in boot.img recovery.img logo.bin lk.bin preloader*.bin cache.img secro.img EBR1 MBR MT*.txt; do
    if [ -f "$BASE_ROM_DIR/$file" ]; then
        echo_info "Copying $file..."
        cp "$BASE_ROM_DIR/$file" "$WORK_DIR/base/"
    fi
done
    
cd "$WORK_DIR/base"
ABS_BASE_DIR=$(pwd)

SYSTEM_IMG=$(convert_sparse_to_raw "$ABS_BASE_DIR/system.img")
USERDATA_IMG=$(convert_sparse_to_raw "$ABS_BASE_DIR/userdata.img")

# Track if images were converted (for cleanup/renaming later)
SYSTEM_WAS_SPARSE=false
USERDATA_WAS_SPARSE=false
if [ "$SYSTEM_IMG" != "$ABS_BASE_DIR/system.img" ]; then
    SYSTEM_WAS_SPARSE=true
fi
if [ "$USERDATA_IMG" != "$ABS_BASE_DIR/userdata.img" ]; then
    USERDATA_WAS_SPARSE=true
fi

# Mount system image
echo_info "Mounting system image..."
if ! mount -t ext4 -o loop "$SYSTEM_IMG" "$ABS_MOUNT_SYS" 2>&1; then
    LOOP_SYS=$(losetup --find --show "$SYSTEM_IMG" 2>&1)
    if [ -n "$LOOP_SYS" ]; then
        mount -t ext4 "$LOOP_SYS" "$ABS_MOUNT_SYS" || exit 1
    else
        echo_error "Failed to mount system image"
        exit 1
    fi
fi

# Mount userdata image
echo_info "Mounting userdata image..."
if ! mount -t ext4 -o loop "$USERDATA_IMG" "$ABS_MOUNT_USER" 2>&1; then
    LOOP_USER=$(losetup --find --show "$USERDATA_IMG" 2>&1)
    if [ -n "$LOOP_USER" ]; then
        mount -t ext4 "$LOOP_USER" "$ABS_MOUNT_USER" || exit 1
    else
        echo_error "Failed to mount userdata image"
        exit 1
    fi
fi

# Install APK to system
echo_info "Installing APK to system partition..."
ABS_WORKSPACE=$(cd /workspace && pwd)
cp "$ABS_WORKSPACE/build/kidz.apk" "$ABS_MOUNT_SYS/app/${PACKAGE_NAME}.apk"
chmod 644 "$ABS_MOUNT_SYS/app/${PACKAGE_NAME}.apk"
chown root:root "$ABS_MOUNT_SYS/app/${PACKAGE_NAME}.apk"

# # Enable ADB by default
# echo_info "Enabling ADB by default..."
# BUILD_PROP="$ABS_MOUNT_SYS/build.prop"

# # Remove existing ADB-related properties if they exist
# sed -i '/^ro.adb.secure=/d' "$BUILD_PROP"
# sed -i '/^persist.sys.usb.config=/d' "$BUILD_PROP"
# sed -i '/^ro.debuggable=/d' "$BUILD_PROP"

# # Add ADB configuration at the end of build.prop
# echo "" >> "$BUILD_PROP"
# echo "# ADB enabled by default" >> "$BUILD_PROP"
# echo "ro.adb.secure=0" >> "$BUILD_PROP"
# echo "persist.sys.usb.config=adb" >> "$BUILD_PROP"
# echo "ro.debuggable=1" >> "$BUILD_PROP"
# echo_info "  ADB enabled in build.prop"

# Install custom launch script and setup install-recovery.sh
echo_info "Setting up launch script and install-recovery.sh..."

ABS_WORKSPACE=$(cd /workspace && pwd)

# Copy app switcher script to /system/bin/
LAUNCH_SCRIPT_SRC="$ABS_WORKSPACE/app_switcher.sh"
LAUNCH_SCRIPT_DST="$ABS_MOUNT_SYS/bin/app_switcher.sh"
echo_info "  Installing app switcher script to /system/bin/app_switcher.sh"
cp "$LAUNCH_SCRIPT_SRC" "$LAUNCH_SCRIPT_DST"
chmod 755 "$LAUNCH_SCRIPT_DST"
chown root:shell "$LAUNCH_SCRIPT_DST" 2>/dev/null || chown root:root "$LAUNCH_SCRIPT_DST"

# Copy install-recovery.sh from project root
INSTALL_RECOVERY_SRC="$ABS_WORKSPACE/install-recovery.sh"
INSTALL_RECOVERY_DST="$ABS_MOUNT_SYS/etc/install-recovery.sh"
echo_info "  Installing install-recovery.sh to /system/etc/install-recovery.sh"
cp "$INSTALL_RECOVERY_SRC" "$INSTALL_RECOVERY_DST"
chmod 755 "$INSTALL_RECOVERY_DST"
chown root:shell "$INSTALL_RECOVERY_DST" 2>/dev/null || chown root:root "$INSTALL_RECOVERY_DST"

# Install xxd binary
XXD_BINARY="/usr/local/bin/xxd"
echo_info "Installing xxd binary..."
cp "$XXD_BINARY" "$ABS_MOUNT_SYS/bin/xxd"
chmod 755 "$ABS_MOUNT_SYS/bin/xxd"
chown root:shell "$ABS_MOUNT_SYS/bin/xxd" 2>/dev/null || chown root:root "$ABS_MOUNT_SYS/bin/xxd"
echo_info "  xxd binary installed"

# Create app data directories
echo_info "Setting up app data directories..."
mkdir -p "$ABS_MOUNT_USER/data/$PACKAGE_NAME"
chmod 755 "$ABS_MOUNT_USER/data/$PACKAGE_NAME"
# Use numeric UID/GID for system user (1000:1000 is standard in Android)
chown 1000:1000 "$ABS_MOUNT_USER/data/$PACKAGE_NAME" 2>/dev/null || chown root:root "$ABS_MOUNT_USER/data/$PACKAGE_NAME"

# Unmount images
echo_info "Unmounting images..."
umount "$ABS_MOUNT_SYS" 2>/dev/null || true
umount "$ABS_MOUNT_USER" 2>/dev/null || true

# Clean up loop devices if they were created
losetup -D 2>/dev/null || true

# Remove trap before creating zip (so cleanup doesn't interfere)
trap - EXIT

# Create ROM zip with all ROM files
echo_info "Creating rom.zip..."
cd "$ABS_WORK_DIR/base"

# Rename modified raw images back to original names for the ROM zip
if [ "$SYSTEM_WAS_SPARSE" = true ] && [ -f "system_raw.img" ]; then
    # Remove original sparse image and rename raw to original name
    rm -f system.img
    mv system_raw.img system.img
fi
if [ "$USERDATA_WAS_SPARSE" = true ] && [ -f "userdata_raw.img" ]; then
    # Remove original sparse image and rename raw to original name
    rm -f userdata.img
    mv userdata_raw.img userdata.img
fi

zip -j ../rom.zip ./*
cd "$ABS_WORK_DIR"
# Move to build directory
mv rom.zip /workspace/build/rom.zip

# Clean up work directory
cd /workspace
rm -rf "$WORK_DIR"

echo_info "ROM zip created: build/rom.zip"


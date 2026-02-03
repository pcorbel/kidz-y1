#!/system/bin/sh
# App switcher script - toggles between com.innioasis.y1 and com.kidz.y1

INPUT_DEVICE="/dev/input/event2"
STATE_FILE="/data/local/tmp/back_button_state"
PACKAGE_STOCK="com.innioasis.y1"
PACKAGE_KIDZ="com.kidz.y1"
ACTIVITY_STOCK="com.innioasis.y1/com.innioasis.y1.activity.MainActivity"
ACTIVITY_KIDZ="com.kidz.y1/com.kidz.y1.activities.MainActivity"

# Wait for boot completion
while [ "$(getprop sys.boot_completed)" != "1" ]; do
    sleep 1
done

log -p i -t app_switcher "Starting script"

# Clean up state file on exit
trap "rm -f $STATE_FILE" EXIT

# Initialize state
BACK_DOWN=0
BACK_START_SEC=0
echo "0:0" > "$STATE_FILE"

# Function to reverse byte order (little-endian to big-endian)
reverse_bytes() {
    hex="$1"
    result=""
    len=${#hex}
    i=$((len - 2))
    while [ $i -ge 0 ]; do
        result="${result}${hex:$i:2}"
        i=$((i - 2))
    done
    echo "$result"
}

# Function to get current active package
get_current_package() {
    focus_line=$(dumpsys window windows | grep -E 'mCurrentFocus')
    if echo "$focus_line" | grep -q "com\.innioasis\.y1"; then
        echo "com.innioasis.y1"
    elif echo "$focus_line" | grep -q "com\.kidz\.y1"; then
        echo "com.kidz.y1"
    else
        echo ""
    fi
}

# Function to toggle between apps
toggle_app() {
    current_pkg=$(get_current_package)
    
    if [ "$current_pkg" = "$PACKAGE_STOCK" ]; then
        log -p i -t app_switcher "Switching from stock to kidz app"
        am start -n "$ACTIVITY_KIDZ" 2>/dev/null
        am force-stop "$PACKAGE_STOCK" 2>/dev/null
    elif [ "$current_pkg" = "$PACKAGE_KIDZ" ]; then
        log -p i -t app_switcher "Switching from kidz to stock app"
        am start -n "$ACTIVITY_STOCK" 2>/dev/null
        am force-stop "$PACKAGE_KIDZ" 2>/dev/null
    fi
}

# Background timer checker - checks state file every second
(
    while true; do
        sleep 1
        if [ -f "$STATE_FILE" ]; then
            state=$(cat "$STATE_FILE" 2>/dev/null)
            BACK_DOWN_FILE="${state%%:*}"
            BACK_START_SEC_FILE="${state#*:}"
            
            if [ "$BACK_DOWN_FILE" = "1" ] && [ "$BACK_START_SEC_FILE" != "0" ]; then
                current_time=$(date +%s)
                hold_time=$((current_time - BACK_START_SEC_FILE))
                if [ "$hold_time" -ge "5" ]; then
                    log -p i -t app_switcher "Back button held for 5 seconds, toggling app!"
                    toggle_app
                    echo "0:0" > "$STATE_FILE"
                    sleep 2
                fi
            fi
        fi
    done
) &
TIMER_PID=$!

# Main loop: read raw input events
while true; do
    # Only check buttons if the screen is on
    if dumpsys power | grep -q "mScreenOn=true"; then
        event_bytes=$(dd if="$INPUT_DEVICE" bs=16 count=1 2>/dev/null | xxd -p)
        [ -z "$event_bytes" ] && break
        
        # Extract timestamp (seconds only) - bytes 0-3 (hex positions 0-7)
        sec_hex=$(reverse_bytes ${event_bytes:0:8})
        sec=$((0x$sec_hex))
        
        # Extract event type, code, and value
        type_hex=$(reverse_bytes ${event_bytes:16:4})
        code_hex=$(reverse_bytes ${event_bytes:20:4})
        value_hex=$(reverse_bytes ${event_bytes:24:8})
        
        type=$((0x$type_hex))
        value=$((0x$value_hex))
        code=$((0x$code_hex))
        
        # Only process key events (type 1)
        if [ "$type" -eq 1 ]; then
            # Track BACK key (158)
            if [ "$code" = "158" ]; then
                if [ "$value" = "1" ]; then
                    # Back pressed
                    if [ "$BACK_START_SEC" -eq 0 ]; then
                        BACK_START_SEC=$(date +%s)
                        BACK_DOWN=1
                        echo "$BACK_DOWN:$BACK_START_SEC" > "$STATE_FILE"
                    fi
                else
                    # Back released
                    if [ "$BACK_START_SEC" -ne 0 ]; then
                        BACK_START_SEC=0
                        BACK_DOWN=0
                        echo "$BACK_DOWN:$BACK_START_SEC" > "$STATE_FILE"
                    fi
                fi
            fi
        fi
    else
        # Reset state when screen is off
        if [ "$BACK_START_SEC" -ne 0 ]; then
            BACK_START_SEC=0
            BACK_DOWN=0
            echo "$BACK_DOWN:$BACK_START_SEC" > "$STATE_FILE"
        fi
    fi
done

# Kill timer process if main loop exits
kill $TIMER_PID 2>/dev/null
rm -f "$STATE_FILE"


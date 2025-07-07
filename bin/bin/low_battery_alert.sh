#!/bin/bash

# Path to your battery info.
# On most laptops, this is BAT0. You can verify by checking:
# ls /sys/class/power_supply/
BATTERY_PATH="/sys/class/power_supply/BAT0"
ALERT_PERCENTAGE=15
NOTIFIED_LOW=false # Flag to ensure we only notify once per low battery cycle

# --- Function to get battery info ---
get_battery_info() {
    if [ -d "$BATTERY_PATH" ]; then
        # Preferred method: direct file reading
        CAPACITY=$(cat "$BATTERY_PATH/capacity")
        STATUS=$(cat "$BATTERY_PATH/status")
        echo "${CAPACITY}% (${STATUS})"
    elif command -v acpi &> /dev/null; then
        # Fallback method: using acpi command
        # You might need to install acpi: sudo pacman -S acpi
        ACPI_OUTPUT=$(acpi -b | grep -oP '\d+%' | head -n 1) # Get percentage
        ACPI_STATUS=$(acpi -b | grep -oP '(?<=: ).*?(?=,)' | head -n 1) # Get status
        echo "${ACPI_OUTPUT} (${ACPI_STATUS})"
    else
        echo "Error: Battery information not found or 'acpi' command not available."
        exit 1
    fi
}
# --- End of function ---

while true; do
    BAT_INFO_STRING=$(get_battery_info)
    
    # Extract capacity and status from the string returned by get_battery_info
    # Example: "85% (Discharging)" -> CAPACITY=85, STATUS=Discharging
    CAPACITY=$(echo "$BAT_INFO_STRING" | grep -oP '^\d+')
    STATUS=$(echo "$BAT_INFO_STRING" | grep -oP '(?<=\().*?(?=\))')

    if [ "$STATUS" = "Discharging" ] && [ "$CAPACITY" -le "$ALERT_PERCENTAGE" ] && [ "$NOTIFIED_LOW" = false ]; then
        notify-send -u critical -a "battery" "LOW BATTERY!" "Your battery is at ${CAPACITY}%! Please plug in your charger."
        NOTIFIED_LOW=true # Set flag to true after notifying
    elif [ "$STATUS" != "Discharging" ] || [ "$CAPACITY" -gt "$ALERT_PERCENTAGE" ]; then
        NOTIFIED_LOW=false # Reset flag if battery is charging or above threshold
    fi

    sleep 60 # Check battery every 60 seconds
done

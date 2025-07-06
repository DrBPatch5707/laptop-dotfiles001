!#/bin/bash
tempeture=$(echo "$(acpi -t | rg 'Thermal 1')" | rg -oP '\d+\.\d+')
echo $tempeture
exit
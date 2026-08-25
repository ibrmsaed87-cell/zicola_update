#!/bin/bash
ICON="iconapp.png"
RES="app/src/main/res"

# Generate Adaptive Foreground
NODPI="$RES/drawable-nodpi"
mkdir -p "$NODPI"
# 288x288 is exactly 72dp at xxxhdpi, perfectly matching the circular safe zone diameter.
convert "$ICON" -resize 288x288 -background none -gravity center -extent 432x432 "$NODPI/ic_launcher_foreground.png"

echo "Foreground generated with 288x288 (maximum safe zone diameter)"

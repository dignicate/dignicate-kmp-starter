#!/bin/bash

set -euo pipefail

# Icon generation module for KMP project
# Requires ImageMagick (magick command)

generate_icons() {
  local env=$1
  local project_root=$2

  if [[ ! "$env" =~ ^(dev|stg|prd)$ ]]; then
    echo "[!] Invalid environment: $env. Must be one of dev, stg, prd."
    return 1
  fi

  local icon_dir="$project_root/icons/$env"
  local base_icon="$icon_dir/icon.png"
  local foreground_icon="$icon_dir/foreground.png"
  local background_icon="$icon_dir/background.png"

  echo
  echo "--- Generating icons for environment: $env ---"

  # 1. Check if source images exist
  for img in "$base_icon" "$foreground_icon" "$background_icon"; do
    if [ ! -f "$img" ]; then
      echo "[!] Source image not found: $img"
      return 1
    fi
  done

  # 2. Check for ImageMagick
  local im_cmd=""
  if command -v magick &> /dev/null; then
    im_cmd="magick"
  elif command -v convert &> /dev/null; then
    im_cmd="convert"
  else
    echo "[!] ImageMagick is not installed."
    echo "    On macOS, you can install it with: brew install imagemagick"
    return 1
  fi

  # 3. Android Icons (Adaptive & Legacy)
  echo "Generating Android icons..."
  local android_res_dir="$project_root/composeApp/src/androidMain/res"

  # Adaptive Icons (Android 8.0+)
  local adaptive_dir="$android_res_dir/mipmap-anydpi-v26"
  mkdir -p "$adaptive_dir"
  mkdir -p "$android_res_dir/drawable"

  # Generate foreground/background (1080x1080)
  $im_cmd "$foreground_icon" -resize 1080x1080 "$android_res_dir/drawable/ic_launcher_foreground.png"
  $im_cmd "$background_icon" -resize 1080x1080 "$android_res_dir/drawable/ic_launcher_background.png"

  # Adaptive XML
  local adaptive_xml='<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>'

  echo "$adaptive_xml" > "$adaptive_dir/ic_launcher.xml"
  echo "$adaptive_xml" > "$adaptive_dir/ic_launcher_round.xml"

  # Legacy Icons
  local android_icons=(
    "mipmap-mdpi|48x48"
    "mipmap-hdpi|72x72"
    "mipmap-xhdpi|96x96"
    "mipmap-xxhdpi|144x144"
    "mipmap-xxxhdpi|192x192"
  )

  for item in "${android_icons[@]}"; do
    IFS="|" read -r dir size <<< "$item"
    local target_dir="$android_res_dir/$dir"
    mkdir -p "$target_dir"
    $im_cmd "$base_icon" -resize "$size" "$target_dir/ic_launcher.png"
    $im_cmd "$base_icon" -resize "$size" "$target_dir/ic_launcher_round.png"
  done

  # 4. iOS Icons
  echo "Generating iOS icons..."
  local ios_app_dir="$project_root/iosApp"
  local ios_icon_dir=$(find "$ios_app_dir" -name "AppIcon.appiconset" -type d -print -quit)

  if [ -z "$ios_icon_dir" ]; then
    echo "[!] Could not find AppIcon.appiconset in $ios_app_dir"
    return 1
  fi

  # iOS App Icon sizes (filename|size)
  local ios_icons=(
    "icon-20x20@2x.png|40x40"
    "icon-20x20@3x.png|60x60"
    "icon-29x29@1x.png|29x29"
    "icon-29x29@2x.png|58x58"
    "icon-29x29@3x.png|87x87"
    "icon-40x40@2x.png|80x80"
    "icon-40x40@3x.png|120x120"
    "icon-60x60@2x.png|120x120"
    "icon-60x60@3x.png|180x180"
    "icon-20x20-ipad@1x.png|20x20"
    "icon-20x20-ipad@2x.png|40x40"
    "icon-29x29-ipad@1x.png|29x29"
    "icon-29x29-ipad@2x.png|58x58"
    "icon-40x40-ipad@1x.png|40x40"
    "icon-40x40-ipad@2x.png|80x80"
    "icon-76x76-ipad@1x.png|76x76"
    "icon-76x76-ipad@2x.png|152x152"
    "icon-83.5x83.5-ipad@2x.png|167x167"
    "icon-1024x1024@1x.png|1024x1024"
  )

  for item in "${ios_icons[@]}"; do
    IFS="|" read -r filename size <<< "$item"
    $im_cmd "$base_icon" -resize "$size" "$ios_icon_dir/$filename"
  done

  # Handle Contents.json (Generate only if missing)
  if [ ! -f "$ios_icon_dir/Contents.json" ]; then
    echo "Creating missing Contents.json..."
    cat <<EOF > "$ios_icon_dir/Contents.json"
{
  "images" : [
    { "size" : "20x20", "idiom" : "iphone", "filename" : "icon-20x20@2x.png", "scale" : "2x" },
    { "size" : "20x20", "idiom" : "iphone", "filename" : "icon-20x20@3x.png", "scale" : "3x" },
    { "size" : "29x29", "idiom" : "iphone", "filename" : "icon-29x29@1x.png", "scale" : "1x" },
    { "size" : "29x29", "idiom" : "iphone", "filename" : "icon-29x29@2x.png", "scale" : "2x" },
    { "size" : "29x29", "idiom" : "iphone", "filename" : "icon-29x29@3x.png", "scale" : "3x" },
    { "size" : "40x40", "idiom" : "iphone", "filename" : "icon-40x40@2x.png", "scale" : "2x" },
    { "size" : "40x40", "idiom" : "iphone", "filename" : "icon-40x40@3x.png", "scale" : "3x" },
    { "size" : "60x60", "idiom" : "iphone", "filename" : "icon-60x60@2x.png", "scale" : "2x" },
    { "size" : "60x60", "idiom" : "iphone", "filename" : "icon-60x60@3x.png", "scale" : "3x" },
    { "size" : "20x20", "idiom" : "ipad", "filename" : "icon-20x20-ipad@1x.png", "scale" : "1x" },
    { "size" : "20x20", "idiom" : "ipad", "filename" : "icon-20x20-ipad@2x.png", "scale" : "2x" },
    { "size" : "29x29", "idiom" : "ipad", "filename" : "icon-29x29-ipad@1x.png", "scale" : "1x" },
    { "size" : "29x29", "idiom" : "ipad", "filename" : "icon-29x29-ipad@2x.png", "scale" : "2x" },
    { "size" : "40x40", "idiom" : "ipad", "filename" : "icon-40x40-ipad@1x.png", "scale" : "1x" },
    { "size" : "40x40", "idiom" : "ipad", "filename" : "icon-40x40-ipad@2x.png", "scale" : "2x" },
    { "size" : "76x76", "idiom" : "ipad", "filename" : "icon-76x76-ipad@1x.png", "scale" : "1x" },
    { "size" : "76x76", "idiom" : "ipad", "filename" : "icon-76x76-ipad@2x.png", "scale" : "2x" },
    { "size" : "83.5x83.5", "idiom" : "ipad", "filename" : "icon-83.5x83.5-ipad@2x.png", "scale" : "2x" },
    { "size" : "1024x1024", "idiom" : "ios-marketing", "filename" : "icon-1024x1024@1x.png", "scale" : "1x" }
  ],
  "info" : {
    "version" : 1,
    "author" : "xcode"
  }
}
EOF
  fi

  echo "Icon generation completed."
}

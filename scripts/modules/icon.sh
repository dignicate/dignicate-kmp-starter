#!/bin/bash

set -euo pipefail

# Icon generation module for KMP project
# Requires ImageMagick (convert or magick command)

generate_icons() {
  local env=$1
  local project_root=$2
  local base_image="$project_root/config/icons/$env/base.png"

  echo
  echo "--- Generating icons for environment: $env ---"

  # 1. Check if base image exists
  if [ ! -f "$base_image" ]; then
    echo "[!] Base image not found: $base_image"
    echo "    Please place your 1024x1024 base.png in config/icons/$env/"
    return 1
  fi

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

  # 3. Android Icons
  echo "Generating Android icons..."
  local android_res_dir="$project_root/composeApp/src/androidMain/res"

  # Sizes for Android (standard launcher icons)
  declare -A android_sizes=(
    ["mipmap-mdpi"]="48x48"
    ["mipmap-hdpi"]="72x72"
    ["mipmap-xhdpi"]="96x96"
    ["mipmap-xxhdpi"]="144x144"
    ["mipmap-xxxhdpi"]="192x192"
  )

  for dir in "${!android_sizes[@]}"; do
    local size="${android_sizes[$dir]}"
    local target_dir="$android_res_dir/$dir"
    mkdir -p "$target_dir"

    # Generate ic_launcher.png and ic_launcher_round.png
    $im_cmd "$base_image" -resize "$size" "$target_dir/ic_launcher.png"
    $im_cmd "$base_image" -resize "$size" "$target_dir/ic_launcher_round.png"
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
    $im_cmd "$base_image" -resize "$size" "$ios_icon_dir/$filename"
  done

  # 5. Handle Contents.json
  # Create if missing, but do not overwrite if exists.
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

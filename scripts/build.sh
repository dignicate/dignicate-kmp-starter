#!/bin/bash

set -euo pipefail

# Calculate script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Move to project root to ensure relative paths in gradlew work as expected
cd "$PROJECT_ROOT"

# === 1. 環境チェック ===
echo "=== Environment Check ==="

if ! command -v java &> /dev/null; then
  echo "[!] Java is not installed. Please install JDK 17 or higher."
  exit 1
fi

if [ ! -f "./gradlew" ]; then
  echo "[!] gradlew not found in the root directory: $PROJECT_ROOT"
  exit 1
fi

chmod +x gradlew

# Android SDK のパスチェック
if [ -f "local.properties" ]; then
  SDK_DIR=$(grep sdk.dir local.properties | cut -d'=' -f2 | xargs)
  if [ -n "$SDK_DIR" ]; then
    export ANDROID_HOME="$SDK_DIR"
    export PATH="$PATH:$ANDROID_HOME/platform-tools"
  fi
fi

# Support a non-interactive mode
CODEGEN_ONLY=0
if [[ "${1:-}" == "--codegen-only" || "${1:-}" == "-c" ]]; then
  CODEGEN_ONLY=1
fi

./gradlew --version

echo
echo "=== KMP Project Build Script ==="

# === 2. 環境（ENV/FLAVOR）選択 ===
echo
echo "Select environment:"
echo "  1 -> Production"
echo "  2 -> Staging"
echo "  3 -> Development"
echo
read -rp "Select (1-3): " env_input

case "$env_input" in
  1)
    ENV="prod"
    GRADLE_ARGS=("-Penv=prod")
    ;;
  2)
    ENV="stg"
    GRADLE_ARGS=("-Penv=stg")
    ;;
  3)
    ENV="dev"
    GRADLE_ARGS=("-Penv=dev")
    ;;
  *)
    echo "Cancelled."
    exit 1
    ;;
esac

# === 3. 設定ファイルの同期 ===
echo
echo "--- Syncing Configuration Files ---"

# Load icon generation module using SCRIPT_DIR
source "$SCRIPT_DIR/modules/icon.sh"

# Generate icons for the selected environment, passing PROJECT_ROOT
generate_icons "$ENV" "$PROJECT_ROOT"

# === 4. クリーンアップ ===
echo
if [ "$CODEGEN_ONLY" -eq 0 ]; then
  read -rp "Run './gradlew clean'? (y/N) [N]: " run_clean
else
  run_clean="y"
fi

if [[ "$run_clean" =~ ^[Yy]$ ]]; then
  echo "Running clean..."
  ./gradlew clean
  echo "Clean completed."
fi

if [ "$CODEGEN_ONLY" -eq 1 ]; then
  exit 0
fi

# === 5. ビルド / 実行 ===
echo
echo "Select build type:"
echo "  1 -> Android (aab)"
echo "  2 -> Android (apk)"
echo "  3 -> iOS (Simulator Build)"
echo "  else -> Run on Device"
echo
read -rp "Select: " build_input

case "$build_input" in
  1)
    ./gradlew :composeApp:bundleRelease "${GRADLE_ARGS[@]}"
    ;;
  2)
    ./gradlew :composeApp:assembleDebug "${GRADLE_ARGS[@]}"
    ;;
  3)
    if [[ "$(uname)" != "Darwin" ]]; then
      echo "[!] iOS build requires macOS."
      exit 1
    fi
    xcodebuild -project iosApp/kmpstarter/kmpstarter.xcodeproj -scheme kmpstarter -configuration Debug -sdk iphonesimulator build
    ;;
  *)
    # デバイス選択
    echo "Scanning for available devices..."
    device_entries=()

    # --- Android デバイス検出 ---
    if command -v adb &> /dev/null; then
      adb_out=$(adb devices | grep -v "List of devices attached" || true)
      while read -r line; do
        [ -z "$line" ] && continue
        id=$(echo "$line" | awk '{print $1}')
        state=$(echo "$line" | awk '{print $2}')
        [ "$state" != "device" ] && continue

        # 詳細情報を取得 (< /dev/null で while read の stdin を守る)
        model=$(adb -s "$id" shell getprop ro.product.model < /dev/null 2>/dev/null | xargs | tr '_' ' ' || echo "Unknown")
        version=$(adb -s "$id" shell getprop ro.build.version.release < /dev/null 2>/dev/null | xargs || echo "Unknown")
        api=$(adb -s "$id" shell getprop ro.build.version.sdk < /dev/null 2>/dev/null | xargs || echo "Unknown")

        is_emulator=false
        if [[ "$id" == emulator-* ]]; then
          is_emulator=true
        fi

        type_label=""
        [ "$is_emulator" = true ] && type_label=" (emulator)"

        connection_label=""
        if [[ "$id" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+:[0-9]+$ ]]; then
          connection_label=" (wireless)"
        fi

        device_entries+=("$id|$model$connection_label (mobile), Android $version (API $api)$type_label|android")
      done <<< "$adb_out"
    fi

    # --- iOS デバイス検出 (macOS のみ) ---
    if [[ "$(uname)" == "Darwin" ]] && command -v xcrun &> /dev/null; then
      ios_sims=$(xcrun simctl list devices | grep "(Booted)" || true)
      while IFS= read -r line; do
        [ -z "$line" ] && continue
        line=$(echo "$line" | xargs)
        if [[ $line =~ (.*)\ \(([0-9A-F-]+)\)\ \(Booted\) ]]; then
          name="${BASH_REMATCH[1]}"
          id="${BASH_REMATCH[2]}"
          runtime_id=$(xcrun simctl list devices | grep -B 10 "$id" | grep "iOS" | tail -n 1 | sed -E 's/-- (.*) --/\1/' | sed 's/iOS/com.apple.CoreSimulator.SimRuntime.iOS/' | sed 's/ /-/g' | sed 's/\./-/g' || echo "")
          device_entries+=("$id|$name (mobile), $runtime_id (simulator)|ios-sim")
        fi
      done <<< "$ios_sims"

      ios_devices=$(xcrun xctrace list devices 2>/dev/null | grep -v "SDKs" | grep "([0-9A-F]" || true)
      while IFS= read -r line; do
        [ -z "$line" ] && continue
        if [[ $line =~ ^(.*)\ \(([0-9A-F]{8}-[0-9A-F]{16}|[0-9A-F]{40})\) ]]; then
           full_info="${BASH_REMATCH[1]}"
           id="${BASH_REMATCH[2]}"
           is_duplicate=false
           for entry in "${device_entries[@]}"; do
             if [[ "$entry" == *"$id"* ]]; then is_duplicate=true; break; fi
           done
           if [ "$is_duplicate" = false ]; then
             name=$(echo "$full_info" | sed -E 's/ \(.*\)$//')
             os_info=$(echo "$full_info" | grep -oE '\([0-9.]+\)' | tr -d '()' || echo "iOS")
             device_entries+=("$id|$name (mobile), iOS $os_info (physical)|ios-device")
           fi
        fi
      done <<< "$ios_devices"
    fi

    if [ ${#device_entries[@]} -eq 0 ]; then
      echo "[!] No running Android or iOS devices found."
      exit 1
    fi

    echo "Select device to run:"
    for i in "${!device_entries[@]}"; do
      IFS='|' read -r id info type <<< "${device_entries[$i]}"
      echo "  $((i+1)) -> $info ($id)"
    done
    echo "  q -> Cancel"
    echo
    read -rp "Select (1-${#device_entries[@]}): " device_idx

    if [[ "$device_idx" == "q" ]]; then
      echo "Cancelled."
      exit 0
    fi

    if [[ ! "$device_idx" =~ ^[0-9]+$ ]] || [ "$device_idx" -lt 1 ] || [ "$device_idx" -gt "${#device_entries[@]}" ]; then
      echo "Invalid selection."
      exit 1
    fi

    IFS='|' read -r selected_id selected_info selected_type <<< "${device_entries[$((device_idx-1))]}"

    if [ "$selected_type" == "ios-device" ]; then
      echo
      echo "[!] Running on physical iOS devices via CLI is not fully supported."
      echo "Please use Xcode to run on physical devices."
      exit 1
    fi

    if [ "$selected_type" == "android" ]; then
      CMD="./gradlew :composeApp:installDebug ${GRADLE_ARGS[*]}"
    elif [ "$selected_type" == "ios-sim" ]; then
      CMD="xcodebuild -project iosApp/kmpstarter/kmpstarter.xcodeproj -scheme kmpstarter -configuration Debug -sdk iphonesimulator -destination \"id=$selected_id\" build"
    fi

    echo
    echo "Ready to deploy to: $selected_info"
    echo "Command: $CMD"
    echo
    read -rp "Run directly or copy to clipboard? [c/r] (default: r): " action
    action=${action:-r}

    if [ "$action" = "c" ]; then
      if command -v pbcopy &> /dev/null; then
        printf "%s" "$CMD" | pbcopy
        echo "Command copied to clipboard."
      else
        echo "pbcopy not found. Command:"
        echo "$CMD"
      fi
      exit 0
    else
      echo "Executing: $CMD"
    fi

    if [ "$selected_type" == "android" ]; then
      ./gradlew :composeApp:installDebug "${GRADLE_ARGS[@]}"
      echo "Starting app on $selected_id..."
      adb -s "$selected_id" shell am start -n com.dignicate.kmpstarter/com.dignicate.kmpstarter.MainActivity
    elif [ "$selected_type" == "ios-sim" ]; then
      echo "Building for iOS Simulator ($selected_id)..."
      xcodebuild -project iosApp/kmpstarter/kmpstarter.xcodeproj \
                 -scheme kmpstarter \
                 -configuration Debug \
                 -sdk iphonesimulator \
                 -destination "id=$selected_id" \
                 build

      APP_SETTINGS=$(xcodebuild -project iosApp/kmpstarter/kmpstarter.xcodeproj -scheme kmpstarter -configuration Debug -sdk iphonesimulator -showBuildSettings)
      APP_PATH=$(echo "$APP_SETTINGS" | grep -m 1 "BUILT_PRODUCTS_DIR" | awk '{print $3}')
      APP_NAME=$(echo "$APP_SETTINGS" | grep -m 1 "EXECUTABLE_FOLDER_PATH" | awk '{print $3}')

      echo "Installing on simulator..."
      xcrun simctl install "$selected_id" "$APP_PATH/$APP_NAME"
      echo "Launching com.dignicate.kmpstarter..."
      xcrun simctl launch "$selected_id" com.dignicate.kmpstarter
    fi
    ;;
esac

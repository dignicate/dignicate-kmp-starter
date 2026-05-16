# dignicate-kmp-starter
A practical Kotlin Multiplatform starter template for building Android and iOS apps with Compose Multiplatform. Includes common app structure such as navigation, tabs, drawer, and settings.

## Architecture

This project follows a layered Clean Architecture for Kotlin Multiplatform. For details on module structure, layer responsibilities, and implementation patterns (UseCase, ViewModel, Compose), see:

- [Architecture Guidelines](docs/architecture-guidelines.md)

## Build Flavors / Environments

The app builds for three environments (`dev`, `stg`, `prod`) with distinct application/bundle identifiers and a compile-time `APP_ENV` value surfaced in the Debug Menu. See:

- [Build Flavors](docs/build-flavors.md)

Quick reference:

```bash
# Android
./gradlew :composeApp:assembleDevDebug
./gradlew :composeApp:installStgDebug
./gradlew :composeApp:bundleProdRelease

# iOS — pick a shared scheme: kmpstarter-{dev,stg,prod}
xcodebuild -project iosApp/kmpstarter/kmpstarter.xcodeproj \
           -scheme kmpstarter-dev \
           -configuration Debug-Dev \
           -sdk iphonesimulator build
```

## Getting Started

### Interactive Build Script

```bash
./scripts/build.sh
```

Prompts for environment (`prod` / `stg` / `dev`), then for build type (Android AAB / APK, iOS Ad-Hoc / App Store IPA, or run on a connected device / simulator). It regenerates icons for the chosen env and invokes the matching Gradle task or `xcodebuild` scheme.

### Environment-Specific Icons

Source images live under `icons/{prod,stg,dev}/` (`icon.png`, `foreground.png`, `background.png`). The icon module (`scripts/modules/icon.sh`) resizes them into the Android `mipmap-*` and iOS `AppIcon.appiconset` outputs.

To regenerate icons only (without a build):

```bash
./scripts/build.sh --codegen-only
```

**Note**: icon generation overwrites a shared `composeApp/src/androidMain/res/` and the single iOS `AppIcon.appiconset`, so only one environment's icons are baked in at a time. Per-flavor source sets is an open follow-up — see [Build Flavors §6](docs/build-flavors.md#6-open-follow-ups).

**Prerequisites:**
- ImageMagick: `brew install imagemagick`

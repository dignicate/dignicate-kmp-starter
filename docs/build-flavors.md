# Build Flavors / Environments

The project supports three environments — `dev`, `stg`, `prod` — across both Android and iOS. Each environment compiles with a distinct application/bundle identifier and produces a different `AppConfig.env` value at runtime, surfaced in the Debug Menu footer.

## 1. Env value flow

Build system constant → `AppConfig` (constructed at platform entry point) → Koin `single` → `DebugMenuViewModel.UiState`.

```
Android: BuildConfig.APP_ENV (per flavor)
            ↓
        MainActivity.buildAppConfig()
            ↓
        AppEnvironment.fromName(...)
            ↓
        AppConfig (Koin single)
            ↓
        DebugMenuViewModel.uiState

iOS:    xcconfig APP_ENV → Info.plist $(APP_ENV) substitution
            ↓
        MainViewControllerFactory.buildAppConfig() (reads NSBundle.infoDictionary["APP_ENV"])
            ↓
        AppEnvironment.fromName(...)
            ↓
        AppConfig (Koin single)
            ↓
        DebugMenuViewModel.uiState
```

## 2. Android

### Flavors

Defined in `composeApp/build.gradle.kts`:

| Flavor | `applicationIdSuffix` | `versionNameSuffix` | `BuildConfig.APP_ENV` |
|---|---|---|---|
| `dev` | `.dev` | `-dev` | `"dev"` |
| `stg` | `.stg` | `-stg` | `"stg"` |
| `prod` | (none) | (none) | `"prod"` |

This yields six Gradle tasks per kind: `assembleDevDebug`, `assembleDevRelease`, `assembleStgDebug`, …, `assembleProdRelease`.

### Switching in IDE

Android Studio / IntelliJ → **Build → Select Build Variant…** → pick a flavor + build type from the panel (e.g. `devDebug`).

### CLI

```bash
./gradlew :composeApp:assembleDevDebug          # install-ready APK for dev
./gradlew :composeApp:installDevDebug           # install on connected device
./gradlew :composeApp:bundleProdRelease         # AAB for production
```

### Parallel install

Because each flavor has a distinct `applicationId` (e.g. `com.dignicate.kmpstarter.dev`), `dev` / `stg` / `prod` can coexist on the same device.

## 3. iOS

### Configurations

Defined in `iosApp/kmpstarter/kmpstarter.xcodeproj` via `scripts/setup_ios_flavors.rb`:

| Configuration | xcconfig | `APP_ENV` | Bundle ID suffix | `KOTLIN_FRAMEWORK_BUILD_TYPE` |
|---|---|---|---|---|
| `Debug-Dev` | `Config-Dev.xcconfig` | `dev` | `.dev` | `debug` |
| `Debug-Stg` | `Config-Stg.xcconfig` | `stg` | `.stg` | `debug` |
| `Debug-Prod` | `Config-Prod.xcconfig` | `prod` | (empty) | `debug` |
| `Release-Dev` | `Config-Dev.xcconfig` | `dev` | `.dev` | `release` |
| `Release-Stg` | `Config-Stg.xcconfig` | `stg` | `.stg` | `release` |
| `Release-Prod` | `Config-Prod.xcconfig` | `prod` | (empty) | `release` |
| `Debug` (legacy) | `Config-Prod.xcconfig` | `prod` | (empty) | — |
| `Release` (legacy) | `Config-Prod.xcconfig` | `prod` | (empty) | — |

The two legacy `Debug` / `Release` Configurations are kept for compatibility; they now resolve to `prod` via the attached xcconfig.

`PRODUCT_BUNDLE_IDENTIFIER` is `com.dignicate.kmpstarter$(BUNDLE_ID_SUFFIX)` across every Configuration, so the suffix from xcconfig drives the final value.

`KOTLIN_FRAMEWORK_BUILD_TYPE` is required because the Kotlin Gradle Plugin's `embedAndSignAppleFrameworkForXcode` task cannot infer the Kotlin framework build type from non-default Configuration names.

### Schemes

Three shared schemes in `xcshareddata/xcschemes/`:

| Scheme | Run / Test / Analyze | Profile / Archive |
|---|---|---|
| `kmpstarter-dev` | `Debug-Dev` | `Release-Dev` |
| `kmpstarter-stg` | `Debug-Stg` | `Release-Stg` |
| `kmpstarter-prod` | `Debug-Prod` | `Release-Prod` |

### Switching in Xcode

Top-left scheme dropdown → pick `kmpstarter-{dev,stg,prod}`. The Configuration is selected automatically per action (Run vs Archive).

### CLI

```bash
xcodebuild -project iosApp/kmpstarter/kmpstarter.xcodeproj \
           -scheme kmpstarter-dev \
           -configuration Debug-Dev \
           -sdk iphonesimulator \
           -destination "generic/platform=iOS Simulator" build
```

## 4. Adding a new environment

Example: adding `qa`.

1. **Common**: add `QA("qa")` to `AppEnvironment` and extend `fromName()`.
2. **Android**: add a new entry in `composeApp/build.gradle.kts`:
   ```kotlin
   create("qa") {
       dimension = "environment"
       applicationIdSuffix = ".qa"
       versionNameSuffix = "-qa"
       buildConfigField("String", "APP_ENV", "\"qa\"")
   }
   ```
3. **iOS**:
   - Create `iosApp/kmpstarter/Configs/Config-Qa.xcconfig`.
   - Add `"Qa" => "Configs/Config-Qa.xcconfig"` to `ENV_TO_XCCONFIG` and the matching `Debug-Qa` / `Release-Qa` rows to `NEW_CONFIGS` in `scripts/setup_ios_flavors.rb`.
   - Re-run `ruby scripts/setup_ios_flavors.rb`.

## 5. `scripts/setup_ios_flavors.rb`

Idempotent Ruby script that applies the iOS Configuration / scheme structure to the pbxproj. Re-run whenever:

- the env list changes
- the pbxproj is regenerated (e.g. after a major Xcode upgrade strips custom settings)
- new file references need to be attached

Requires the `xcodeproj` Ruby gem:

```bash
gem install xcodeproj
ruby scripts/setup_ios_flavors.rb
```

The script never deletes existing Configurations or schemes; it only adds or updates settings on known names.

## 6. Open follow-ups

- Icon generation (`scripts/build.sh` + `scripts/modules/icon.sh`) currently writes into `composeApp/src/androidMain/res/`, shared across flavors, so only one environment's icons are baked in at a time. Producing per-flavor icons requires moving the output into `composeApp/src/<flavor>/res/` source sets. The iOS side has the same constraint: a single `AppIcon.appiconset` is overwritten regardless of the selected scheme.
- Release signing per flavor (separate keystores) is not configured. All flavors currently sign with the debug keystore for `Release` builds.

# dignicate-kmp-starter
A practical Kotlin Multiplatform starter template for building Android and iOS apps with Compose Multiplatform. Includes common app structure such as navigation, tabs, drawer, and settings.

## Getting Started

### Build Script
You can use the provided build script to handle environment switching and icon generation.

```bash
./scripts/build.sh
```

### Environment-Specific Icons
Icons are automatically generated based on the selected environment (`prd`, `stg`, `dev`).
1. Place your 1024x1024 base image at `config/icons/{env}/base.png`.
2. Run `./scripts/build.sh` and select the environment.
3. The script will generate icons for both Android and iOS using ImageMagick.

**Prerequisites:**
- ImageMagick: `brew install imagemagick`

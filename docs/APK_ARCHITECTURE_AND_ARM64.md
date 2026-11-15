# Android ARM64 APK Architecture & Build Verification

This document specifies the Android APK build configuration, targeting the 64-bit ARM architecture (`arm64-v8a`) across physical devices (including Google Pixel 8 Pro, Pixel 10 Pro XL, and modern 64-bit Android hardware) while maintaining cross-platform and emulator compatibility.

---

## 1. ABI Configuration (`arm64-v8a`)

Both Gradle build configurations (`/app/build.gradle.kts` and `/android/app/build.gradle`) declare explicit NDK ABI filters:

```kotlin
ndk {
    abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
}
```

### Native Library Verification in the Compiled APK:
Inside the output APK archive, the native JNI libraries for 64-bit ARM are bundled under `lib/arm64-v8a/`:
- `lib/arm64-v8a/libandroidx.graphics.path.so`
- `lib/arm64-v8a/libdatastore_shared_counter.so`

This ensures full native execution without binary translation on:
- Google Pixel 7, 8, 9, 10 Series (Tensor G-series 64-bit only architecture)
- Qualcomm Snapdragon 8 Gen 1/2/3/4 & 64-bit ARM SoC devices
- MediaTek Dimensity 64-bit architectures

---

## 2. GitHub Actions CI/CD (`.github/workflows/build-android-apk.yml`)

The automated workflow on push/PR:
1. Builds the signed release APK using Gradle.
2. Formats and computes SHA-384 checksums for:
   - `tyr-protocol-mobile-v3.1.apk` (Universal release APK with `arm64-v8a`)
   - `tyr-protocol-mobile-v3.1-arm64.apk` (ARM64 release artifact)
3. Uploads both APKs and their SHA-384 digests to GitHub Actions artifacts (`tyr-protocol-mobile-v3.1-arm64-apk`).

---

## 3. Local / CLI Build Commands

- **Debug APK**:
  ```bash
  gradle assembleDebug
  # Output: app/build/outputs/apk/debug/app-debug.apk
  ```

- **Release APK**:
  ```bash
  gradle assembleRelease
  # Output: app/build/outputs/apk/release/app-release-unsigned.apk (or signed via signingConfig)
  ```

- **Verify ABI Architecture**:
  ```bash
  unzip -l app/build/outputs/apk/debug/app-debug.apk | grep "lib/arm64-v8a"
  ```

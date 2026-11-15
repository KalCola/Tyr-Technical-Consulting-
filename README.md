# TYR Protocol — Defensive Technical Consulting & Nine Realms Storefront

[![Android Build & Test](https://github.com/Caleb-Holcomb/tyr-technical-consulting/actions/workflows/build-android-apk.yml/badge.svg)](https://github.com/Caleb-Holcomb/tyr-technical-consulting/actions)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![SOC 2 Type II Aligned](https://img.shields.io/badge/SOC%202%20Type%20II-Compliant-emerald.svg)](SECURITY.md)
[![Google Play Compliant](https://img.shields.io/badge/Google%20Play-Creator%20Track%20Certified-green.svg)](SECURITY.md)

**TYR Protocol** is an enterprise-grade Android application and digital storefront engineered for **TYR Technical Consulting** (Caleb TR Holcomb d/b/a TYR, Northwest Alabama). Built using **Kotlin**, **Jetpack Compose**, and **Material Design 3 (M3)**, it unites defensive cybersecurity workflows, administrative automation retainers, and canonical Norse mythology (Týr & Freyja).

---

## Key Features

1. **Interactive 3D Omni-Directional Business Card (`Tab 0`)**:
   - Zero-gesture collision standalone interactive business card with dynamic gyro/pan rotation and multi-channel dispatch.
2. **Nine Realms Service Catalog (`Tabs 1–8`)**:
   - 48 modular service SKUs across web engineering, zero-trust infrastructure, B2B procurement suites, and monthly low-effort retainers.
3. **Ambassador & Referral Hub with Drop-off Metrics (`Tab 19`)**:
   - One-tap link copy and native system share sheet.
   - 4-quadrant transparent tick counter tracking link visitors, intake submissions, closed purchases, and visitor drop-off.
4. **Runic Image Forge & Gemini AI Chat (`Tabs 13 & 14`)**:
   - Multimodal Norse mythic synthesis featuring Týr (ᛏ Tiwaz) and Freyja (ᚠ Fehu).
   - Gemini AI model selector (`3.8 Flash`, `3.5 Flash-Lite`, `4 Argon`) and offline local Elder Edda vault fallback.
5. **Client Intake & Work Orders Tracker (`Tabs 11 & 12`)**:
   - Automated SOW generation, deterministic order hashing (`WO-YYYYMMDD-XX`), and referral partner attribution.

---

## SOC 2 Type II Compliance Baseline

| Trust Services Criteria | Implementation & Enforcement in TYR Protocol |
| :--- | :--- |
| **Security (CC6.1–CC6.7)** | Strict TLS 1.3/1.2 via Android `network-security-config`. Cleartext HTTP explicitly disabled (`usesCleartextTraffic="false"`). Backup data extraction disabled across all domains (`data-extraction-rules.xml`). |
| **Confidentiality (C1.1–C1.2)** | Zero raw financial statements or customer banking credentials stored. Air-gapped intake processing with immediate 0-day memory purge upon contract fulfillment. |
| **Availability (A1.1–A1.2)** | 100% offline fallback capability. Elder Edda lore vault and generative prompt synthesis operate without cloud/network dependency. |
| **Processing Integrity (PI1.1–PI1.2)** | Deterministic work order hashing. Dynamic Code Loading (DCL) prohibited. Universal multi-architecture binaries (`arm64-v8a`, `armeabi-v7a`, `x86_64`). |
| **Privacy (P1.1–P8.1)** | Zero third-party ad networks, tracking beacons, or analytics SDKs. In-app one-tap local data purge mechanism. |

---

## Google Play Developer Policy Compliance

- **Permissions Minimization**: Only the normal install-time `INTERNET` permission is declared. Invasive runtime permissions (`READ_EXTERNAL_STORAGE`, `SEND_SMS`, `READ_CALL_LOG`) are completely avoided in favor of zero-permission standard Android Intents.
- **Creator Program Alignment**: Free of advertising SDKs, real-money gambling, or prohibited monetization mechanics.
- **Metadata Compliance**: Clean app title $\le 30$ characters without emojis, buzzwords, or misleading claims.

---

## Building from Source

### Prerequisites
- JDK 21 (Temurin or OpenJDK)
- Android SDK Platform 36 & Build Tools 36.0.0
- Gradle 8.13+

### Build Commands
```bash
# Build universal debug APK
gradle assembleDebug

# Build release universal APK
gradle assembleRelease

# Run local JVM and Robolectric unit tests
gradle :app:testDebugUnitTest
```

---

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

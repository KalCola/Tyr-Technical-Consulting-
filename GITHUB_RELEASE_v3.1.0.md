# TYR Protocol v3.1.0 — Unified Command Hub, Billing Defense & Zero-Custody Storefront

**Release Tag:** `v3.1.0`  
**Target Commitish:** `main`  
**Application ID:** `pro.tyrprotocol.storefront`  
**Developer:** Caleb TR Holcomb d/b/a TYR (Northwest Alabama & Nationwide)  
**Date:** November 2026  

---

## 🚀 Release Highlights

### 1. 🧭 Master Command Hub & Quick Directory
* **One-Tap Discoverability:** Added a centralized modal command center accessible from any screen via the top app bar, bottom navigation, or deck tabs.
* **Instant Runic Search:** Search, filter, and jump directly into any of the 22 tools, catalog pages, or intake workflows without scrolling through long horizontal chip lists.
* **Four Structured Pillars:**
  * 🛍️ *Storefront & Service Catalog* (Pages 1–8 & All Realms Explorer)
  * ⚡ *Pricing, Estimates & Quotes* (Custom Quote Builder, Estimate Cart, Payment Clarity)
  * 📋 *Client Operations* (Intake Onboarding, Live Work Order Tracker, Digital E-Sign, Zero-Custody Vault)
  * 📡 *Intelligence & Channels* (Gemini AI Systems Advisor, 1-on-1 Discovery Scheduler, Google Chat Relay)

### 2. 💳 Payment Clarity & SOW Billing Defense
* **Zero-Surprise Billing Oath:** Distinct contractual separation between **100% Owned One-Time Capital Deliverables** ($0/month forever) and **Active Month-to-Month Retainers** (billed on the 1st of each month).
* **Dispute Defense SOW Clause Generator:** Interactive tool that generates plain-English contractual clauses for any catalog SKU or custom task, with a 1-click clipboard copy button to stamp directly onto quotes and commercial invoices.
* **Billing FAQs:** Comprehensive defense against customer misunderstandings ("I thought that was a one-time payment not every month").

### 3. 🛠️ Custom Bespoke Quote Builder
* **Unlisted Scope Plug & Play:** Clients can plug and play bespoke tasks, automation scripts, and server hardening services not listed in the standard catalog.
* **Billing Model Selector:** Tag individual custom tasks as either `⚡ One-Time Fee` or `🔄 Monthly Retainer`.
* **Integrated SOW Export:** Export complete Statement of Work summaries to clipboard or pre-formatted email drafts.

### 4. 🧮 Dual-Bucket Estimate Calculator
* **Segregated Subtotals:** Replaces single blended totals with clear, separate cards for **⚡ ONE-TIME CAPITAL** ($0/mo ongoing) and **🔄 RECURRING MONTHLY** (billed 1st of month).
* **Live Reactive Cart Badge:** Real-time badge counter in the top bar and bottom navigation showing active items.

### 5. 🛡️ Security, SOC 2 & Privacy Hardening
* **Zero-Custody Guarantee:** Raw banking statements and customer working files submitted for financial audits are ephemeral and purged within 24 hours of report delivery.
* **Data Extraction Security:** `android:allowBackup="false"` and `data_extraction_rules.xml` strictly prohibit cloud and device-transfer leakage across root, database, and shared preferences.
* **Strict TLS Enforcement:** Cleartext HTTP traffic completely disabled (`usesCleartextTraffic="false"`).
* **Zero Advertising / Tracking SDKs:** Free of third-party telemetry, ad beacons, and surveillance libraries.

---

## 📦 Distribution Packages

| File | Description | Target Platforms |
| :--- | :--- | :--- |
| `tyr-protocol-v3.1.0-release.apk` | Universal Standalone Android Application Package | Android 7.0 to Android 15 (`arm64-v8a`, `armeabi-v7a`, `x86_64`) |
| `tyr-protocol-v3.1.0-release.aab` | Google Play Store Distribution App Bundle | Google Play Console production track |
| `checksums-sha384.txt` | Cryptographic integrity verification ledger | SHA-384 hashes for release artifacts |

---

## 🔒 Cryptographic Verification & Integrity

To verify binary integrity prior to installation or deployment:

```bash
# Verify SHA-384 checksum on Linux / macOS:
sha384sum tyr-protocol-v3.1.0-release.apk

# Verify on Windows (PowerShell):
Get-FileHash -Algorithm SHA384 tyr-protocol-v3.1.0-release.apk
```

---

## 🛠️ Build Information
* **Compile SDK:** 36 (Android 15)
* **Target SDK:** 36
* **Min SDK:** 24 (Android 7.0+)
* **Build System:** Gradle Kotlin DSL (`build.gradle.kts`)
* **Framework:** Jetpack Compose + Material Design 3 (M3)
* **License:** Apache License 2.0

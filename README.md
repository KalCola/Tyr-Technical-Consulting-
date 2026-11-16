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
   - 36 effort-calibrated service SKUs across 14 Norse & Dwarven realms across web engineering, zero-trust infrastructure, B2B procurement suites, and monthly low-effort retainers.
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
---

## Entity & Production Infrastructure

| Attribute | Production Value |
| :--- | :--- |
| **Legal Entity & DBA** | `CALEB TR HOLCOMB d/b/a TYR` (`TYR Technical Consulting` / `Tyr's Technical Consultation`) |
| **Primary Classification** | `NAICS 541512` (Computer Systems Design Services) & `NAICS 541611` (Administrative & General Management Consulting) |
| **Tax Identification** | `EIN: 42-XXX-X843` (IRS Form W-9 provided upon commercial engagement) |
| **Headquarters** | Beaverton, Northwest Alabama (`35544`) — 100% Remote Delivery |
| **Live Storefront Domain** | `tyrprotocol.pro` (GitHub Pages + Custom `CNAME` + TLS 1.3) |
| **Payment & Settlement Stack** | Stripe Checkout (`acct_1Ub2VYRxlL3JK5Uq`) with SHA-384 receipt hashing (`js/stripe-checkout.js`), milestone split billing (`100%` / `50%` / `33%` / `25%`), and ACH settlement to Lili Business Banking |

---

## Architecture & Repository File Map (v3.2.0-dwarven-forge)

The TYR Protocol operates across three synchronized execution surfaces:

1. **Static Web Storefront (Root)**: Zero-build HTML5/CSS3/ES2022 single-page deck (`index.html`, `404.html`, `manifest.webmanifest`, `sw.js`, `css/`, `js/`, `data/catalog.json`, `assets/3d_card.html`) deployed directly to GitHub Pages.
2. **Native Android Application (`app/`)**: Kotlin + Jetpack Compose + Material 3 universal APK (`pro.tyrprotocol.storefront`) bundling the offline web storefront and native views (`StorefrontApp.kt`, `CatalogData.kt`, `NorseImageForgeView.kt`, `OmniBusinessCard.kt`).
3. **Interactive React/TypeScript SPA (`src/`)**: Modular B2B & retainer portal components (`src/App.tsx`, `src/dwarven-forge.css`, `EnterpriseB2BPortal.tsx`, `ClientRetainerPortal.tsx`, `ZeroCustodyDocumentVaultModal.tsx`).

| Path / Directory | Role & Purpose |
| :--- | :--- |
| `index.html` | Primary 23-page (`P0–P22`) zero-build storefront deck shell |
| `css/base.css` | Core typography, CSS custom properties, and reset tokens |
| `css/deck.css` | Multi-page deck layout, realm themes, and card styling |
| `css/responsive.css` | Mobile, tablet, and desktop viewport breakpoints |
| `js/deck.js` | Page navigation controller (`P0–P22`), keyboard/swipe deck engine |
| `js/app.js` | Catalog hydration, intake forms, work-order generator, and UI state |
| `js/stripe-checkout.js` | Stripe Checkout session builder, split-billing, and SHA-384 receipt hashing |
| `js/forensics.js` | Client-side field forensics, DNS/SPF/DMARC diagnostics, and audit tools |
| `js/growth-kit.js` | Ambassador referral attribution and conversion/drop-off metrics |
| `data/catalog.json` | Canonical `v3.2.0-dwarven-forge` catalog (14 realms, 36 SKUs, Pages 1–10) |
| `data/skus/*.json` | Modular per-SKU metadata and deliverable specifications |
| `assets/3d_card.html` | Interactive 3D Omni-Directional Business Card (`P0`) |
| `_headers` | Security headers (CSP, HSTS, X-Frame-Options, Referrer-Policy) |
| `app/src/main/java/com/example/tyrconsulting/` | Native Android Jetpack Compose source (`StorefrontApp.kt`, `CatalogData.kt`, `NorseImageForgeView.kt`, `OmniBusinessCard.kt`) |
| `.github/workflows/pages-verify.yml` | CI syntax verification (`node --check`) and `catalog.json` schema validation |
| `.github/workflows/jekyll-gh-pages.yml` | GitHub Pages static deployment workflow |
| `.github/workflows/build-android-apk.yml` | Universal Android APK CI build workflow |
| `.github/workflows/release-build.yml` | Tagged release packaging and artifact publication |
| `.github/workflows/stale.yml` | Automated repository issue/PR lifecycle management |

---

## 14-Realm & Dwarven Forge Palette Tokens

| Realm | Rune | Primary Hex | Secondary Hex | Domain Focus |
| :--- | :---: | :---: | :---: | :--- |
| `01. Midgard` | — | `#dc2626` | `#f87171` | Mortal Realm / Web Foundations |
| `02. Asgard` | — | `#d97706` | `#fbbf24` | Godly Citadel / Email & Domain Shield |
| `03. Vanaheim` | — | `#059669` | `#34d399` | Nature & Flow / Intake & QR Systems |
| `04. Alfheim` | — | `#0284c7` | `#38bdf8` | Light & Intellect / Career & Bids |
| `05. Nidavellir` | — | `#ea580c` | `#fb923c` | Dwarven Forge / Ironclad Contracts |
| `06. Muspelheim` | — | `#e11d48` | `#fb7185` | Fire & Energy / Buyer Dossiers & Ads |
| `07. Svartalfheim` | — | `#9333ea` | `#c084fc` | Subterranean Wealth / Ledgers & Audits |
| `08. Niflheim` | — | `#0d9488` | `#2dd4bf` | Primordial Mists / Vaults & Privacy Freeze |
| `09. Jotunheim` | — | `#4f46e5` | `#818cf8` | Giants & Titans / Dispute Kit & FOSS Freedom |
| `10. Yggdrasil` | — | `#d97706` | `#fde047` | Cosmic Ash / Total Ecosystem Retainer |
| `11. Sindri's Hearth` | `ᛊ` | `#d97706` | `#fbbf24` | Dwarven Forge • Rapid DNS & SPF Quick-Lock |
| `12. Brokkr's Bellows` | `ᚦ` | `#c2410c` | `#fb923c` | Master Bellows • Custom Domain Email Alias |
| `13. Ívaldi's Workshop` | `ᛚ` | `#0891b2` | `#22d3ee` | Precision Craft • Fillable Forms & Starter Pages |
| `14. Dvalinn's Vault` | `ᛞ` | `#7c3aed` | `#a78bfa` | Runic Artisan • High-Speed Link-in-Bio Hub |
---

## Complete 36-SKU Service Catalog Matrix ($65.00 Minimum Price Floor)

| Page | SKU Code | Service Title | Realm | Billing Type | v3.2.0 Price |
| :---: | :--- | :--- | :--- | :--- | :---: |
| `P1` | `SVC-WEB-01` | Midgard Website Launch | `01. Midgard` | Fixed-Scope Core | `$265` |
| `P1` | `SVC-DOM-02` | Asgard Domain & Email Shield | `02. Asgard` | Defensive Security | `$175` |
| `P1` | `SVC-KIT-03` | Vanaheim QR & Form Kit | `03. Vanaheim` | Digital Intake | `$165` |
| `P2` | `SVC-CAR-04` | Alfheim Resume & Bid Overhaul | `04. Alfheim` | Executive Asset | `$165` |
| `P2` | `SVC-OPS-05` | Nidavellir 3-Doc E-Sign Kit | `05. Nidavellir` | Contract Vault | `$195` |
| `P2` | `SVC-BUY-06` | Muspelheim Buyer's Dossier | `06. Muspelheim` | One-Time Dossier | `$175` |
| `P3` | `SVC-CMD-07` | Svartalfheim Budget & Schedule C Hub | `07. Svartalfheim` | Zero-Custody Ledger | `$195` |
| `P3` | `SVC-VLT-08` | Niflheim Insurance & Legacy Vault | `08. Niflheim` | Encrypted Vault | `$175` |
| `P3` | `SVC-AUD-09` | Jotunheim Billing & ISP Dispute Kit | `09. Jotunheim` | Consumer Defense | `$195` |
| `P4` | `MOD-DNS-03` | Mjölnir DNS & SPF Quick-Lock | `11. Sindri's Hearth` | 24-Hr Quick-Lock | `$65` |
| `P4` | `MOD-EML-04` | Gungnir Custom Email Alias Setup | `12. Brokkr's Bellows` | Custom Address | `$75` |
| `P4` | `MOD-FRM-05` | Draupnir Single Fillable PDF or QR Card | `13. Ívaldi's Workshop` | 1-Doc / QR Build | `$85` |
| `P4` | `WEB-LP-01` | Gullinbursti Single-Page Bio & Link Hub | `14. Dvalinn's Vault` | Single-Page Hub | `$115` |
| `P4` | `WEB-ST-02` | Skíðblaðnir 3-Section Starter Landing Page | `13. Ívaldi's Workshop` | 3-Section Site | `$185` |
| `P4` | `SVC-LOK-10` | Loki Digital Flyer & Ad Pack | `06. Muspelheim` | Visual Arsenal | `$95 / $135` |
| `P4` | `SVC-GRM-11` | Garm Privacy Scrub & Freeze Shield | `08. Niflheim` | Privacy Shield | `$115 / $155` |
| `P4` | `SVC-FEN-12` | Fenrir Free-Software Switch Kit | `09. Jotunheim` | FOSS Liberation | `$135 / $175` |
| `P5` | `SUB-WEB-01` | Web & QR Care Retainer | `01. Midgard` | Monthly Retainer | `$65–$85 / mo` |
| `P5` | `SUB-DOM-02` | Domain & Email Shield Retainer | `02. Asgard` | Security Retainer | `$75–$95 / mo` |
| `P5` | `SUB-OPS-03` | Document & Vault Caretaker | `05. Nidavellir` | Ops Retainer | `$85–$110 / mo` |
| `P6` | `SUB-FIN-04` | Expense Optimizer Retainer | `07. Svartalfheim` | Finance Retainer | `$95–$135 / mo` |
| `P6` | `SUB-BIL-05` | Bill Watchdog Retainer | `09. Jotunheim` | Watchdog Retainer | `$85–$115 / mo` |
| `P6` | `SUB-ADM-06` | Back-Office Admin Concierge | `04. Alfheim` | Admin Retainer | `$95–$135 / mo` |
| `P6` | `RET-ALL-07` | Yggdrasil Complete Retainer | `10. Yggdrasil` | Sovereign Crown | `$545 Launch \| $185–$245 / mo` |
| `P7` | `SVC-SOP-11` | SOP & Knowledge Base Blueprint | `05. Nidavellir` | One-Time Build | `$175` |
| `P7` | `SVC-SEC-12` | Remote Workstation Hardening & DNS Profile | `02. Asgard` | One-Time Kit | `$155` |
| `P7` | `SVC-TAX-13` | 1099 Contractor Schedule C Ledger | `07. Svartalfheim` | Custom Workbook | `$175` |
| `P7` | `SVC-BCK-14` | Cold-Storage 3-2-1 Backup Runbook | `08. Niflheim` | Automation Kit | `$175` |
| `P7` | `SVC-DCK-15` | Executive Proposal & Pitch Deck Kit | `06. Muspelheim` | Turnkey Templates | `$165` |
| `P8` | `SVC-B2B-16` | B2B Vendor Onboarding & Master MSA Suite | `05. Nidavellir` | Turnkey Suite | `$325` |
| `P8` | `SVC-B2B-17` | Corporate Anti-Phishing & Wire Protocol | `02. Asgard` | Turnkey Governance | `$265` |
| `P9` | `B2B-SEC-01` | B2B Monthly Cyber Hygiene Retainer | `02. Asgard` | Monthly Automated Scan | `$135–$185 / Mo` |
| `P9` | `B2B-SLA-02` | Cloud & SaaS Spend Drift Watchdog | `07. Svartalfheim` | Monthly 30-Min Audit | `$155–$215 / Mo` |
| `P9` | `B2B-LGL-03` | Vendor Contract Renewal Sentinel | `09. Jotunheim` | Automated Tracker | `$110–$150 / Mo` |
| `P9` | `B2B-SEO-04` | B2B Synthetic Uptime & Link Health | `01. Midgard` | 24/7 Synthetic Ping | `$95–$135 / Mo` |
| `P9` | `B2B-SOP-05` | Quarterly SOP Maintenance & Onboarding Desk | `04. Alfheim` | Living Runbook Desk | `$185–$245 / Mo` |
---

## 23-Page Storefront Navigation Directory (P0–P22)

| Page | Section / Surface | Description |
| :---: | :--- | :--- |
| `P0` | **3D Omni Business Card** | Interactive 3D business card (`assets/3d_card.html`) & entry cover |
| `P1` | **Web, Email & QR Foundations** | Core digital presence SKUs (`SVC-WEB-01`, `SVC-DOM-02`, `SVC-KIT-03`) |
| `P2` | **Resumes, Contracts & Buying Dossiers** | Career, e-sign, and dossier SKUs (`SVC-CAR-04`, `SVC-OPS-05`, `SVC-BUY-06`) |
| `P3` | **Budgets, Vaults & Billing Disputes** | Zero-custody ledgers & defense (`SVC-CMD-07`, `SVC-VLT-08`, `SVC-AUD-09`) |
| `P4` | **Dwarven Forge Micro-Services & Tactical Kits** | Rapid quick-locks & kits (`MOD-DNS-03` to `SVC-FEN-12`) |
| `P5` | **Monthly Tech & Vault Caretaking** | Core monthly retainers (`SUB-WEB-01`, `SUB-DOM-02`, `SUB-OPS-03`) |
| `P6` | **Expense Optimization, Bill Watchdog & Retainers** | Finance/admin retainers & Yggdrasil Crown (`SUB-FIN-04` to `RET-ALL-07`) |
| `P7` | **Pure Remote Deliverables** | Non-licensed remote builds (`SVC-SOP-11` to `SVC-DCK-15`) |
| `P8` | **B2B Procurement & Corporate Governance Suite** | Turnkey B2B suites (`SVC-B2B-16`, `SVC-B2B-17`) |
| `P9` | **Monthly Low-Effort B2B Retainers** | Automated B2B retainers (`B2B-SEC-01` to `B2B-SOP-05`) |
| `P10` | **Bifröst 3-Step Workflow & Zero-Custody Covenant** | Delivery workflow & zero-custody architecture overview |
| `P11` | **Client Intake** | Structured project intake and scope capture |
| `P12` | **Work Orders** | Dynamic work-order & SOW generator |
| `P13` | **Estimator** | Real-time scope, SKU, and milestone split estimator |
| `P14` | **Field Forensics** | Client-side DNS, SPF, DMARC, and TLS diagnostic tools (`js/forensics.js`) |
| `P15` | **Runic Image Forge** | Norse & Dwarven visual asset forge |
| `P16` | **Gemini AI Copilot** | AI-assisted scope & technical consultation copilot |
| `P17` | **Payment Clarity** | Stripe Checkout (`js/stripe-checkout.js`), split-billing & SHA-384 receipt hashing |
| `P18` | **Growth Kit** | Conversion & drop-off optimization toolkit (`js/growth-kit.js`) |
| `P19` | **Referrals** | Ambassador referral tracking & attribution |
| `P20` | **About** | Principal profile, credentials & Alabama headquarters |
| `P21` | **Legal** | Commercial terms, MSA, and limitation of liability |
| `P22` | **Privacy Covenant** | Zero-custody data handling & privacy policy |

---

## Local Development, CI/CD Verification & Security Audit Commands

### 1. Local Static Preview Server
```bash
python3 -m http.server 8000
```

### 2. CI/CD Syntax Verification (`pages-verify.yml`)
```bash
node --check js/stripe-checkout.js js/deck.js js/app.js js/forensics.js js/growth-kit.js
```

### 3. Verify 14 Realms & 36 SKUs in `data/catalog.json`
```bash
node -e "
const c = JSON.parse(require('fs').readFileSync('data/catalog.json', 'utf8'));
const realms = Object.keys(c.realms || {});
const skus = (c.pages || []).flatMap(p => p.items || []);
console.log('Version:', c.version);
console.log('Realms:', realms.length);
console.log('SKUs:', skus.length);
if (realms.length !== 14 || skus.length !== 36) process.exit(1);
"
```

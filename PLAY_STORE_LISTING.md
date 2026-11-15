# Google Play Store Listing & Submission Package
**Application:** TYR Protocol  
**Package Name:** `pro.tyrprotocol.storefront`  
**Developer:** Caleb TR Holcomb d/b/a TYR  
**Website:** https://tyrprotocol.pro  
**Contact Email:** tyr@tyrprotocol.pro  
**Support Phone:** +1 (659) 257-8043  

---

## 1. App Details & Store Metadata

### App Title (Exact: 12 / 30 chars)
```text
TYR Protocol
```

### Short Description (Exact: 78 / 80 chars)
```text
Technical consulting, systems engineering, SRE, and bare-metal IT solutions.
```

### Full Description (Character Count: ~2,400 / 4,000 max)
```markdown
TYR Protocol is the executive client portal and service storefront for TYR Technical Consulting, operated by Caleb Holcomb (NAICS 541512 / 541611). Built for small businesses, independent trade contractors, and commercial organizations seeking high-reliability systems engineering, zero-trust cybersecurity, and bare-metal IT operations.

KEY CAPABILITIES & SERVICES:

• 100% OWNED ONE-TIME BUILDS:
Deploy high-performance responsive single-page websites, custom email domain shields, standard operating procedure (SOP) playbooks, and workstation hardening kits. All turnkey deliverables are handed over with complete master file and source code ownership. Zero recurring SaaS hostage fees.

• PROACTIVE CARE PLANS & B2B RETAINERS:
Continuous monthly sentry duty, synthetic 24/7 uptime monitoring, DNS deliverability and SPF/DKIM integrity checks, dark web exposure scanning, and cloud spend drift audits. Transparent month-to-month agreements with 30-day written cancellation.

• CUSTOM BESPOKE QUOTE BUILDER:
Plug and play unlisted technical scopes, custom automation scripts, server setups, or contract redline reviews into an interactive quote builder to generate transparent estimates tailored to your exact operational requirements.

• RADICAL BILLING CLARITY & SOW DEFENSE:
Eliminate billing misunderstandings with explicit contractual classification separating one-time capital purchases from monthly retainers. Generate plain-English Statement of Work (SOW) billing clauses to stamp directly onto quotes and invoices.

• DIGITAL CONTRACT EXECUTION & INTAKE:
Submit formal project requirements via the streamlined intake portal and execute legally binding Statements of Work under UETA and Federal ESIGN standards with cryptographic audit blocks.

• ZERO-CUSTODY PRIVACY GUARANTEE:
Strict adherence to SOC 2 zero-trust principles. Client banking statements and operational working documents submitted for expense or bill audits are treated as ephemeral and purged within 24 hours of report generation. Zero credentials or personal data are retained.

• DIRECT CONSULTING DISCOVERY:
Schedule 1-on-1 discovery calls with Caleb Holcomb for architecture reviews, incident response, and custom bare-metal deployments across Northwest Alabama and nationwide.

Caleb TR Holcomb d/b/a TYR
Computer Systems Design (NAICS 541512)
Administrative & Management Consulting (NAICS 541611)
https://tyrprotocol.pro • tyr@tyrprotocol.pro
```

---

## 2. Store Settings & Categorization

* **App Category:** Business (Secondary: Tools)
* **Tags:** 
  * `Business`
  * `Consulting`
  * `Cybersecurity`
  * `IT Services`
  * `Productivity`
* **Target Audience:** 18 and older (Commercial B2B services, contracts, and systems consulting)
* **Children's Online Privacy Protection Act (COPPA):** Not directed to children under 13.
* **News App:** No.
* **COVID-19 Contact Tracing:** No.
* **Financial Features:** No banking, loans, or cryptocurrency functionality. Provides business consulting and expense audits only.
* **Government Representation:** No affiliation with government entities.

---

## 3. Data Safety Section (Google Play Questionnaire)

### Overview
* **Does your app collect or share user data?** Yes (Only contact data voluntarily submitted through Intake forms for consulting services).
* **Is all of the user data collected by your app encrypted in transit?** Yes (Enforced via HTTPS/TLS 1.3; cleartext traffic is strictly disabled).
* **Do you provide a way for users to request that their data be deleted?** Yes (Direct email request to tyr@tyrprotocol.pro).

### Data Types Collected:
1. **Personal Info -> Name, Email Address, Phone Number:**
   * **Collected?** Yes (Optional, only when submitting an intake form or inquiry).
   * **Shared with third parties?** No. Never sold or shared.
   * **Purpose:** App functionality, client communication, account management, and service fulfillment.
   * **Ephemeral?** Kept only for the duration of the active client engagement.
2. **Financial Info:**
   * **Collected?** None. No credit card numbers, bank accounts, or financial credentials are collected or stored within the app.
3. **Location:** None collected.
4. **Photos / Videos / Files:** None collected (No broad storage permissions requested).
5. **App Info and Performance (Crash logs / Diagnostics):**
   * **Collected?** Yes (Standard Android OS / Google Play crash reports for stability).
   * **Purpose:** Analytics and app reliability.

---

## 4. Content Rating (IARC Questionnaire)

* **Violence:** No
* **Fear:** No
* **Sexuality:** No
* **Gambling:** No
* **Language:** No
* **Controlled Substances:** No
* **User Interactions:** Users can share project inquiries via standard Android email or phone intents. Does not host unmoderated open public chat forums.
* **Resulting Rating:** **Everyone / PEGI 3 / USK 0**

---

## 5. Privacy Policy URL
Submit the following live URL in Google Play Console under **Policy -> Privacy Policy**:
```text
https://tyrprotocol.pro/privacy
```
*(In-app privacy guarantee is also accessible in real time via the Zero-Custody Vault screen and top menu).*

---

## 6. Release Build & AAB Generation

To generate the signed Android App Bundle (`.aab`) for Google Play upload:

1. **AI Studio UI Export:**
   * Open the AI Studio settings/export menu.
   * Select **Download AAB / Export APK**.
2. **CLI Build Command:**
   ```bash
   gradle :app:bundleRelease
   ```
   * Artifact location: `app/build/outputs/bundle/release/app-release.aab`
   * Target SDK: `36` (Android 15)
   * Min SDK: `24` (Android 7.0+)
   * Version Code: `31`
   * Version Name: `3.1.0`

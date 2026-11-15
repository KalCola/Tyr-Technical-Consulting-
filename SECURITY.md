# Security Policy & SOC 2 Compliance

## 1. Compliance Baseline & Zero-Custody Doctrine

**TYR Protocol** is engineered under a strict **Zero-Custody Confidentiality Doctrine** aligned with **AICPA SOC 2 Type II Trust Services Criteria**:

- **Security (CC6.1 - CC6.7)**: Strict Transport Security (TLS 1.3 / TLS 1.2) enforced via Android Network Security Configuration (`cleartextTrafficPermitted="false"`). Backup data extraction is explicitly disabled across all application domains (`data-extraction-rules.xml` and `backup_rules.xml`).
- **Confidentiality (C1.1 - C1.2)**: Zero raw financial statements or customer banking credentials retained. All consultation intake artifacts and client work records are handled in volatile, air-gapped memory and purged upon deliverable acceptance.
- **Availability (A1.1 - A1.2)**: Core services, the Elder Edda lore vault, and generative prompt synthesis maintain 100% offline fallback capabilities with zero external cloud or network lock-in.
- **Processing Integrity (PI1.1 - PI1.2)**: Deterministic work order hashing (`WO-YYYYMMDD-XX`). Dynamic Code Loading (DCL) of external executable bytecode (`.dex`, `.jar`, `.so`) is strictly prohibited.
- **Privacy (P1.1 - P8.1)**: Zero third-party ad networks, tracking beacons, or telemetry harvesting SDKs. The application provides an interactive, one-tap local cache and metric purge mechanism.

---

## 2. Google Play & Android Security Hygiene

- **Least-Privilege Permissions**: The application requests only the install-time `INTERNET` permission. Invasive runtime permissions (such as `READ_EXTERNAL_STORAGE`, `SEND_SMS`, or `READ_CALL_LOG`) are completely absent.
- **Zero Advertising SDKs**: Compliant with the Google Play Creator Program.
- **Clean Metadata**: App titles and descriptors adhere to Google Play Developer Program policies ($\le 30$ characters, no prohibited buzzwords or misleading claims).
- **Universal Multi-Architecture Verification**: Compiled and verified for `arm64-v8a`, `armeabi-v7a`, and `x86_64` native architectures in a single universal binary.

---

## 3. Reporting a Vulnerability

We take the security and integrity of our codebase seriously. If you identify a potential security vulnerability:

1. **Do NOT open a public GitHub issue.**
2. Email full technical details and reproduction steps directly to:
   **colacal43@gmail.com**
3. Include:
   - Affected file, component, or endpoint
   - Step-by-step reproduction instructions or proof-of-concept
   - Impact assessment
4. **Response SLA**:
   - Initial acknowledgement: within 24 hours.
   - Remediation and release: within 72 hours for critical issues.

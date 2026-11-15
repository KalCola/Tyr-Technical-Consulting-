package com.example.tyrconsulting.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class RealmInfo(
    val id: String,
    val number: Int,
    val rune: String,
    val name: String,
    val mainColorHex: Long,
    val lightColorHex: Long,
    val label: String,
    val description: String
)

data class ServiceItem(
    val sku: String,
    val page: Int,
    val cardIndex: Int,
    val realmId: String,
    val runePill: String,
    val meaning: String,
    val title: String,
    val price: String,
    val priceSub: String,
    val attrs: List<Pair<String, String>>,
    val bullets: List<String>,
    val badge: String,
    val specTag: String,
    val isRemoteDeliverable: Boolean = true,
    val requiresLicenseOrCert: Boolean = false
)

data class PresetPackage(
    val id: String,
    val title: String,
    val runeBadge: String,
    val description: String,
    val serviceSkus: List<String>,
    val bundledPrice: String,
    val savings: String,
    val turnaround: String
)

data class BundlePlan(
    val sku: String,
    val title: String,
    val stewardPrice: String,
    val stewardPersonal: String,
    val stewardBusiness: String,
    val techOnlyPass: String,
    val threeRealmLaunch: String,
    val threeRealmSavings: String,
    val features: List<String>
)

data class WorkflowStep(
    val step: Int,
    val title: String,
    val realm: String,
    val description: String,
    val actionLabel: String
)

object TyrCatalog {
    val organizationName = "TYR Technical Consulting"
    val legalName = "CALEB TR HOLCOMB d/b/a TYR"
    val ein = "42-XXX-X843 (CP 575 G)"
    val location = "Northwest Alabama & Nationwide Remote"
    val phone = "(659) 257-8043"
    val phoneRaw = "+16592578043"
    val email = "tyr@tyrprotocol.pro"
    val naicsCodes = listOf("541512 (Computer Systems Design)", "541611 (Admin & Mgmt Consulting)")
    val region = "Northwest Alabama & Nationwide Remote"
    val website = "https://tyrprotocol.pro"

    val realms = listOf(
        RealmInfo("midgard", 1, "ᛗ", "Midgard", 0xFFDC2626, 0xFFF87171, "Websites ($245)", "Realm of Visible Presence • Public Front Door"),
        RealmInfo("asgard", 2, "ᛉ", "Asgard", 0xFFD97706, 0xFFFBBF24, "Email Shield ($165)", "Fortified Citadel • Domain & Email Identity Shield"),
        RealmInfo("vanaheim", 3, "ᚱ", "Vanaheim", 0xFF059669, 0xFF34D399, "QR & Forms ($150)", "Realm of Trade & Harvest • Fast Customer Intake"),
        RealmInfo("alfheim", 4, "ᚨ", "Alfheim", 0xFF0284C7, 0xFF38BDF8, "Resumes & Admin ($145)", "Realm of Light & Clarity • Stand Out From the Crowd"),
        RealmInfo("nidavellir", 5, "ᚷ", "Nidavellir", 0xFFEA580C, 0xFFFB923C, "Contracts ($185)", "Master Forge • Ironclad Written Agreements"),
        RealmInfo("muspelheim", 6, "ᚲ", "Muspelheim", 0xFFE11D48, 0xFFFB7185, "Buyer Guide ($150)", "Fire & Torchlight • Vetted Buyer Research"),
        RealmInfo("svartalfheim", 7, "ᚠ", "Svartalfheim", 0xFF9333EA, 0xFFC084FC, "Expense Opt ($85+)", "Hidden Treasury • Private Offline Wealth Hub"),
        RealmInfo("niflheim", 8, "ᛟ", "Niflheim", 0xFF0D9488, 0xFF2DD4BF, "Home Vault ($155)", "Deep Preservation • Cold-Storage Asset Vault"),
        RealmInfo("jotunheim", 9, "ᚦ", "Jotunheim", 0xFF4F46E5, 0xFF818CF8, "Bill Watch ($75+)", "Giant-Slaying • Defeat Unfair Corporate Bills")
    )

    private val defaultServices = listOf(
        // Page 1
        ServiceItem(
            sku = "SVC-WEB-01",
            page = 1,
            cardIndex = 0,
            realmId = "midgard",
            runePill = "ᛗ MIDGARD",
            meaning = "Realm of Visible Presence • Your Public Front Door",
            title = "Custom Websites & Portfolios",
            price = "$245",
            priceSub = "One-Time Build",
            attrs = listOf("Norse Realm" to "ᛗ Midgard", "Hosting Fee" to "$0 / Month", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Modern, responsive single-page or multi-section design tailored for local businesses and contractors.",
                "Zero mandatory monthly hosting bills — hosted on free high-speed global infrastructure.",
                "Includes master code repository delivery with $0 platform lock-in."
            ),
            badge = "Deliverable: Live Site + Master Code",
            specTag = "HTML5 • QR • Lead Sheet"
        ),
        ServiceItem(
            sku = "SVC-DOM-02",
            page = 1,
            cardIndex = 1,
            realmId = "asgard",
            runePill = "ᛉ ASGARD",
            meaning = "Fortified Citadel • Domain & Email Identity Shield",
            title = "Custom Domain & Protected Email",
            price = "$165",
            priceSub = "One-Time Setup",
            attrs = listOf("Norse Realm" to "ᛉ Asgard", "Protection" to "Anti-Spoof Lock", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Custom domain registration and DNS record provisioning (Cloudflare / Namecheap).",
                "Full anti-spoof email configuration: SPF, DKIM, DMARC, and DNSSEC protection.",
                "Prevents phishing impersonation and stops messages from landing in spam folders."
            ),
            badge = "Deliverable: Domain + Locked Email",
            specTag = "SPF • DKIM • DMARC • DNSSEC"
        ),
        ServiceItem(
            sku = "SVC-KIT-03",
            page = 1,
            cardIndex = 2,
            realmId = "vanaheim",
            runePill = "ᚱ VANAHEIM",
            meaning = "Realm of Trade & Harvest • Fast Customer Intake",
            title = "QR Menus & Online Form Kits",
            price = "$150",
            priceSub = "One-Time Kit",
            attrs = listOf("Norse Realm" to "ᚱ Vanaheim", "Monthly Fee" to "$0 / Month", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Durable vector QR code standees and table tent print-ready PDFs.",
                "Mobile customer intake form linked directly to Google Sheets with real-time email alerts.",
                "No recurring software subscriptions or third-party menu portal fees."
            ),
            badge = "Deliverable: QR Standee + Form + Sheet",
            specTag = "Print PDF • Auto-Sheet"
        ),

        // Page 2
        ServiceItem(
            sku = "SVC-CAR-04",
            page = 2,
            cardIndex = 0,
            realmId = "alfheim",
            runePill = "ᚨ ALFHEIM",
            meaning = "Realm of Light & Clarity • Stand Out From the Crowd",
            title = "Resume & Contractor Bid Overhaul",
            price = "$145",
            priceSub = "One-Time Kit",
            attrs = listOf("Norse Realm" to "ᚨ Alfheim", "Formats" to "Word + PDF", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Modernized, ATS-compliant executive resume with targeted keywords.",
                "Professional contractor bid template for trade quotes and estimates.",
                "Delivered in unlocked Microsoft Word (.docx) and high-res vector PDF."
            ),
            badge = "Deliverable: Resume + Cover + Bid PDF",
            specTag = "ATS-Ready • Word + PDF"
        ),
        ServiceItem(
            sku = "SVC-OPS-05",
            page = 2,
            cardIndex = 1,
            realmId = "nidavellir",
            runePill = "ᚷ NIDAVELLIR",
            meaning = "Master Forge • Ironclad Written Agreements",
            title = "3-Doc Fillable E-Sign Contract Kit",
            price = "$185",
            priceSub = "3-Doc Suite",
            attrs = listOf("Norse Realm" to "ᚷ Nidavellir", "Signing" to "Phone or PC", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Three custom interactive fillable AcroForm PDFs: Work Order, Change Order, and Final Sign-Off.",
                "Legally binding e-signature fields compliant with ESIGN and UETA standards.",
                "Reusable forever on mobile phones, tablets, or desktop PCs without DocuSign fees."
            ),
            badge = "Deliverable: 3 Interactive Fillable PDFs",
            specTag = "AcroForm • ESIGN/UETA"
        ),
        ServiceItem(
            sku = "SVC-BUY-06",
            page = 2,
            cardIndex = 2,
            realmId = "muspelheim",
            runePill = "ᚲ MUSPELHEIM",
            meaning = "Fire & Torchlight • Vetted Buyer Research",
            title = "Vehicle & Gear Buyer's Dossier",
            price = "$150",
            priceSub = "Research Dossier",
            attrs = listOf("Norse Realm" to "ᚲ Muspelheim", "Comparison" to "Up to 3 Models", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Comprehensive buyer's dossier comparing up to 3 candidate vehicles or heavy equipment pieces.",
                "Audit of common failure points, Technical Service Bulletins (TSBs), and NHTSA recalls.",
                "Fair value pricing matrix and point-by-point in-person inspection checklist."
            ),
            badge = "Deliverable: 3-Option Comparison + Checklist",
            specTag = "NHTSA • TSB • Fair Value"
        ),

        // Page 3
        ServiceItem(
            sku = "SVC-CMD-07",
            page = 3,
            cardIndex = 0,
            realmId = "svartalfheim",
            runePill = "ᚠ SVARTALFHEIM",
            meaning = "Hidden Treasury • Private Offline Wealth Hub",
            title = "Home Budget & Business Tracker",
            price = "$195",
            priceSub = "Custom Workbook",
            attrs = listOf("Norse Realm" to "ᚠ Svartalfheim", "Privacy" to "100% Offline", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Self-contained, automated financial tracking workbook built in Excel and Google Sheets.",
                "Categorized income/expense ledgers and Schedule C tax prep summaries.",
                "100% offline privacy — zero cloud bank credential linking or Plaid telemetry."
            ),
            badge = "Deliverable: Custom Excel + Sheets Hub",
            specTag = ".XLSX • Schedule C Ready"
        ),
        ServiceItem(
            sku = "SVC-VLT-08",
            page = 3,
            cardIndex = 1,
            realmId = "niflheim",
            runePill = "ᛟ NIFLHEIM",
            meaning = "Deep Preservation • Cold-Storage Asset Vault",
            title = "Home Insurance & Legacy Vault",
            price = "$155",
            priceSub = "Vault & Binder Kit",
            attrs = listOf("Norse Realm" to "ᛟ Niflheim", "Purpose" to "Storm & Theft", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Structured property inventory ledger formatted for rapid storm or theft insurance claims.",
                "Emergency disaster grab-bag PDF documenting serial numbers, receipts, and policy limits.",
                "Standardized proof-of-loss file structure ready for physical flash drive or safe binder."
            ),
            badge = "Deliverable: Claim Ledger + Emergency PDF",
            specTag = "Proof-of-Loss • Offline"
        ),
        ServiceItem(
            sku = "SVC-AUD-09",
            page = 3,
            cardIndex = 2,
            realmId = "jotunheim",
            runePill = "ᚦ JOTUNHEIM",
            meaning = "Giant-Slaying • Defeat Unfair Corporate Bills",
            title = "Billing, Warranty & ISP Dispute Kit",
            price = "$175",
            priceSub = "Dispute Dossier",
            attrs = listOf("Norse Realm" to "ᚦ Jotunheim", "Goal" to "Bill Credits", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Formal dispute packet targeting utility billing overcharges, telecom rate hikes, or denied warranty claims.",
                "Constructed with statutory legal citations under FCC, UCC, and Fair Credit Billing regulations.",
                "Complete evidence ledger, demand letter PDF, and step-by-step phone negotiation script."
            ),
            badge = "Deliverable: Formal Evidence Dossier PDF",
            specTag = "FCC • UCC • FCRA • Exhibits"
        ),

        // Page 6: Pure Remote Deliverables (No License or Certification Requirements)
        ServiceItem(
            sku = "SVC-SOP-11",
            page = 6,
            cardIndex = 0,
            realmId = "nidavellir",
            runePill = "ᛏ TIWAZ • OPERATIONAL PLAYBOOK",
            meaning = "Sovereign Operations • Standard Operating Procedure (SOP) Vault",
            title = "SOP & Knowledge Base Blueprint",
            price = "$175",
            priceSub = "One-Time Build",
            attrs = listOf("Remote Deliverable" to "100% Digital Turnkey", "Prerequisites" to "Zero Licenses / Certs", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Turnkey Markdown, Obsidian, and Notion knowledge base for solopreneurs, remote teams, and trade contractors.",
                "Standardized core runbooks: client onboarding, emergency escalation tree, and service delivery checklists.",
                "Delivered in open Markdown (.md) and PDF formats with $0 subscription lock-in or recurring SaaS fees."
            ),
            badge = "Deliverable: Markdown Vault + PDF Runbook",
            specTag = "Obsidian/Notion • SOP • 0 License",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "SVC-SEC-12",
            page = 6,
            cardIndex = 1,
            realmId = "asgard",
            runePill = "ᛉ ALGIZ • WORKSTATION FORTRESS",
            meaning = "Sanctuary Citadel • Zero-Trust Remote Workstation Hardening",
            title = "Remote Workstation Hardening & DNS Profile",
            price = "$135",
            priceSub = "One-Time Kit",
            attrs = listOf("Remote Deliverable" to "100% Digital Profiles", "Prerequisites" to "Zero IT Certs Required", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Ready-to-install DNS-over-HTTPS (DoH) / DNSSEC configuration profiles routing through encrypted Quad9 9.9.9.9.",
                "Browser compartmentalization, anti-telemetry guides, and passwordless FIDO2 passkey onboarding walkthrough.",
                "Shields home office workstations against phishing, drive-by malware, and credential stuffing without costly enterprise MDMs."
            ),
            badge = "Deliverable: DoH MobileConfig + Hardening Guide",
            specTag = "Quad9 • DoH • Passkeys • 0 Cert",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "SVC-TAX-13",
            page = 6,
            cardIndex = 2,
            realmId = "svartalfheim",
            runePill = "ᚠ FEHU • SCHEDULE C SHIELD",
            meaning = "Flowing Treasury • 1099 Expense & Tax-Shield Ledger Hub",
            title = "1099 Contractor Schedule C Ledger",
            price = "$165",
            priceSub = "Custom Workbook",
            attrs = listOf("Remote Deliverable" to "100% Offline Ledger", "Prerequisites" to "No CPA License Needed", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Pre-built automated Excel & Google Sheets workbook tailored for independent 1099 contractors and sole proprietors.",
                "Auto-calculating Schedule C business expense deductions, mileage log, and quarterly estimated tax reserve formulas.",
                "Includes fillable W-9 pass-through billing templates with complete privacy — zero bank account linking."
            ),
            badge = "Deliverable: Custom Workbook + W-9 Master",
            specTag = ".XLSX • Schedule C • Tax Reserve",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "SVC-BCK-14",
            page = 6,
            cardIndex = 3,
            realmId = "niflheim",
            runePill = "ᛟ OTHALA • 3-2-1 PRESERVATION",
            meaning = "Ancestral Heritage • Cold-Storage Disaster Recovery Runbook",
            title = "Cold-Storage 3-2-1 Backup Runbook",
            price = "$160",
            priceSub = "Automation Kit",
            attrs = listOf("Remote Deliverable" to "100% Scripted Playbook", "Prerequisites" to "Zero Backup Certs Needed", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Turnkey 3-2-1 backup architecture: 3 copies of business data, on 2 distinct media types, with 1 offsite encrypted archive.",
                "Automated copy-on-write sync scripts (PowerShell & Bash) for external drives and zero-knowledge encrypted cloud remotes (rclone).",
                "Includes periodic restore verification drill checklist to guarantee uncorrupted asset recovery after hardware crashes."
            ),
            badge = "Deliverable: Automation Scripts + DR Manual",
            specTag = "3-2-1 Backup • Rclone • Zero-SaaS",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "SVC-DCK-15",
            page = 6,
            cardIndex = 4,
            realmId = "muspelheim",
            runePill = "ᚲ KAUNAN • HIGH-IMPACT PROPOSAL",
            meaning = "Torch of Insight • Executive High-Conversion Proposal & Pitch Deck",
            title = "Executive Proposal & Pitch Deck Kit",
            price = "$140",
            priceSub = "Turnkey Templates",
            attrs = listOf("Remote Deliverable" to "100% Editable Templates", "Prerequisites" to "Zero Design Skills Needed", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "10-slide high-impact typographic master pitch deck formatted for PowerPoint (.pptx), Google Slides, and vector PDF.",
                "Formal client proposal and quotation document template with embedded ROI calculators and clear scope boundaries.",
                "Engineered for trade contractors and consultants to quote projects with corporate prestige and eliminate scope creep."
            ),
            badge = "Deliverable: PPTX Deck + Proposal PDF",
            specTag = "PowerPoint • Google Slides • PDF",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),

        // Page 4: Subscriptions 1
        ServiceItem(
            sku = "SUB-WEB-01",
            page = 4,
            cardIndex = 0,
            realmId = "midgard",
            runePill = "ᛗ MIDGARD + ᚱ VANAHEIM",
            meaning = "Storefront Guardian • Active Website Upkeep",
            title = "Website & QR Menu Care Plan",
            price = "$55–$75",
            priceSub = "Per Month",
            attrs = listOf("Personal Tier" to "$55 / Month", "Business Tier" to "$75 / Month", "Edits Included" to "Up to 2 / Mo"),
            bullets = listOf(
                "Ongoing maintenance for websites and QR digital menus.",
                "Includes 2 text/content revisions per month, link verification, and intake form checks.",
                "Emergency hotline support for urgent updates or holiday hours changes."
            ),
            badge = "2 Updates/Mo + Form Check",
            specTag = "NAICS 541512 • Web Care"
        ),
        ServiceItem(
            sku = "SUB-DOM-02",
            page = 4,
            cardIndex = 1,
            realmId = "asgard",
            runePill = "ᛉ ASGARD WATCHTOWER",
            meaning = "Bifröst Shield • Continuous Domain & Inbox Watch",
            title = "Domain & Email Shield Plan",
            price = "$65–$85",
            priceSub = "Per Month",
            attrs = listOf("Personal Tier" to "$65 / Month", "Business Tier" to "$85 / Month", "Protection" to "Monthly Scan"),
            bullets = listOf(
                "Continuous automated monitoring of domain expiration, SSL certificates, and DNS records.",
                "Monthly deliverability audits checking SPF/DKIM integrity and email reputation.",
                "Proactive breach alerts if organizational email addresses appear in credential leaks."
            ),
            badge = "Deliverability & Breach Scan",
            specTag = "NAICS 541512 • DNS Shield"
        ),
        ServiceItem(
            sku = "SUB-OPS-03",
            page = 4,
            cardIndex = 2,
            realmId = "niflheim",
            runePill = "ᛟ NIFLHEIM VAULT",
            meaning = "Deep Vault Keeper • Monthly Edits & Recovery",
            title = "Document & Vault Caretaker Plan",
            price = "$75–$95",
            priceSub = "Per Month",
            attrs = listOf("Personal Tier" to "$75 / Month", "Business Tier" to "$95 / Month", "Data Retained" to "Blank Masters"),
            bullets = listOf(
                "Caretaker support for operational fillable forms, inventory records, and contract templates.",
                "Includes 1 template modification per month and permanent cold-storage backup of blank master files.",
                "Instant disaster re-issuance if original files are lost to device crashes."
            ),
            badge = "1 Doc Update/Mo + Recovery",
            specTag = "7-Yr Blank Master Vault"
        ),

        // Page 5: Subscriptions 2
        ServiceItem(
            sku = "SUB-FIN-04",
            page = 5,
            cardIndex = 0,
            realmId = "svartalfheim",
            runePill = "ᚠ SVARTALFHEIM • FEHU",
            meaning = "Treasury Audit • Done-For-You Statement Review",
            title = "Expense & Subscription Optimizer",
            price = "$85–$115",
            priceSub = "Per Month",
            attrs = listOf("Personal / Biz" to "$85 / $115 Mo", "Raw Statements" to "Purged (0-Day)", "Retained File" to "Ledger + Report"),
            bullets = listOf(
                "Done-for-you monthly statement audit identifying unwanted recurring charges and creeping service fees.",
                "Delivered as an actionable executive savings summary and cancellation checklist.",
                "Strict Zero-Custody Guarantee: All client banking statements destroyed within 24 hours of report delivery."
            ),
            badge = "Deliverable: Monthly Savings & Cut Report",
            specTag = "NAICS 541611 • 0 Raw Data Kept"
        ),
        ServiceItem(
            sku = "SUB-BIL-05",
            page = 5,
            cardIndex = 1,
            realmId = "jotunheim",
            runePill = "ᚦ JOTUNHEIM • THURISAZ",
            meaning = "Giant-Slayer Watch • Stop Creeping Vendor Rates",
            title = "Utility & Vendor Bill Watchdog",
            price = "$75–$105",
            priceSub = "Per Month",
            attrs = listOf("Personal / Biz" to "$75 / $105 Mo", "Source Bills" to "Purged (0-Day)", "Retained File" to "Ledger + Report"),
            bullets = listOf(
                "Ongoing rate monitoring across telecom, internet, insurance premiums, and utility invoices.",
                "Detects post-promotional fee hikes and provides scripted negotiation points to roll back rates.",
                "Zero raw statement retention — ephemeral audits ensuring complete financial confidentiality."
            ),
            badge = "Deliverable: Rate-Defense Report + Scripts",
            specTag = "NAICS 541611 • Ephemeral Audit"
        ),
        ServiceItem(
            sku = "SUB-ADM-06",
            page = 5,
            cardIndex = 2,
            realmId = "alfheim",
            runePill = "ᚨ ALFHEIM + ᚷ FORGE",
            meaning = "Back-Office Steward • On-Demand Quotes & Forms",
            title = "Back-Office Admin & Quote Concierge",
            price = "$80–$110",
            priceSub = "Per Month",
            attrs = listOf("Personal / Biz" to "$80 / $110 Mo", "Tasks Included" to "Up to 3 / Mo", "Retained File" to "Ledger + Report"),
            bullets = listOf(
                "On-demand virtual back-office assistant for generating contractor bid estimates, invoices, and PDF forms.",
                "Includes up to 3 custom administrative deliverables per billing cycle with fast 24h turnaround.",
                "Empowers solo operators to look like high-powered corporate enterprises without hiring full-time staff."
            ),
            badge = "Deliverable: 3 Admin Builds/Mo + Report",
            specTag = "NAICS 541611 • 0 Client PII Kept"
        ),

        // Page 7: B2B Procurement & Corporate Governance Services
        ServiceItem(
            sku = "SVC-B2B-16",
            page = 7,
            cardIndex = 0,
            realmId = "nidavellir",
            runePill = "ᚷ GEBO • B2B PARTNERSHIP",
            meaning = "Master Covenant • B2B Vendor Onboarding & MSA Suite",
            title = "B2B Vendor Onboarding & MSA Suite",
            price = "$295",
            priceSub = "4-Doc Master Suite",
            attrs = listOf("B2B Procurement" to "Vendor Packet", "Compliance" to "UETA / ESIGN", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Complete corporate vendor onboarding suite: Master Services Agreement (MSA), SOW template, Mutual NDA, and W-9 pass-through billing ledger.",
                "Interactive fillable AcroForm PDFs with legally binding e-signature fields and audit trail certificate blocks.",
                "Eliminates $2,000+ attorney drafting retainers while establishing ironclad scope boundaries, payment terms, and liability limits."
            ),
            badge = "Deliverable: 4-Doc MSA & Procurement Suite",
            specTag = "MSA • SOW • NDA • Fillable PDF",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "SVC-B2B-17",
            page = 7,
            cardIndex = 1,
            realmId = "asgard",
            runePill = "ᛉ ALGIZ • ANTI-FRAUD CITADEL",
            meaning = "Fortified Citadel • Corporate Anti-Phishing & Wire-Fraud Protocol",
            title = "Corporate Anti-Phishing & Wire Protocol",
            price = "$245",
            priceSub = "Turnkey Governance",
            attrs = listOf("Security Model" to "Zero-Trust Protocol", "Target" to "Staff & Execs", "Turnaround" to "24–48 Hours"),
            bullets = listOf(
                "Battle-tested employee anti-fraud protocol: verbal out-of-band verification SOP for wire transfers and vendor ACH changes.",
                "Executive mailbox anti-spoofing filter rules, shared inbox access standards, and staff security response runbook.",
                "Stops Business Email Compromise (BEC) and invoice diversion schemes that siphon millions from commercial firms."
            ),
            badge = "Deliverable: Anti-Fraud Runbook + Staff SOP",
            specTag = "BEC Defense • Wire Security • Staff SOP",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),

        // Page 8: Monthly Low-Effort B2B Retainers (Automated / High-Margin)
        ServiceItem(
            sku = "B2B-SEC-01",
            page = 8,
            cardIndex = 0,
            realmId = "asgard",
            runePill = "ᛉ ASGARD WATCHTOWER",
            meaning = "Perimeter Sentinel • Automated Cyber Hygiene & Leak Scan",
            title = "B2B Monthly Cyber Hygiene Retainer",
            price = "$125–$175",
            priceSub = "Per Month",
            attrs = listOf("Cadence" to "Monthly Automated Scan", "Effort" to "Automated Scripted Report", "Client Value" to "Executive Assurance"),
            bullets = listOf(
                "Monthly external perimeter scan detecting open exposed ports (RDP, SSH, DB), insecure services, and certificate expirations.",
                "Dark web credential exposure scan across corporate domains and key executive email addresses.",
                "Delivered as a crisp 1-page executive scorecard with pass/fail grades and prioritized remediation steps."
            ),
            badge = "Deliverable: Monthly 1-Page Risk Scorecard",
            specTag = "External Recon • SSL • Dark Web Check",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "B2B-SLA-02",
            page = 8,
            cardIndex = 1,
            realmId = "svartalfheim",
            runePill = "ᚠ FEHU • CLOUD AUDIT",
            meaning = "Treasury Sentry • SaaS & Cloud Spend Drift Watchdog",
            title = "Cloud & SaaS Spend Drift Watchdog",
            price = "$145–$195",
            priceSub = "Per Month",
            attrs = listOf("Cadence" to "Monthly 30-Min Audit", "Avg Savings" to "$350–$1,200 / Mo", "Access Required" to "Read-Only Billing"),
            bullets = listOf(
                "Monthly forensic review of AWS, GCP, Azure, or SaaS billing (Google Workspace, Slack, Zoom, HubSpot, Adobe).",
                "Automatically flags orphaned cloud storage volumes, unassigned licenses, abandoned elastic IPs, and rate creeps.",
                "Provides turnkey cancellation and rightsizing scripts that consistently yield 3x–10x ROI on the retainer cost."
            ),
            badge = "Deliverable: Monthly Cloud Savings Report",
            specTag = "AWS/GCP • SaaS Seats • Drift Audit",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "B2B-LGL-03",
            page = 8,
            cardIndex = 2,
            realmId = "jotunheim",
            runePill = "ᚦ THURISAZ • CONTRACT SHIELD",
            meaning = "Giant-Slaying Defense • Vendor Contract Renewal Sentinel",
            title = "Vendor Contract Renewal Sentinel",
            price = "$95–$135",
            priceSub = "Per Month",
            attrs = listOf("Alert Window" to "30 / 60 / 90 Days", "Capacity" to "Up to 25 Contracts", "Effort" to "Automated Tracker"),
            bullets = listOf(
                "Proactive centralized calendar monitoring for commercial vendor agreements, equipment leases, and enterprise software.",
                "Issues automated 90, 60, and 30-day alerts prior to evergreen auto-renewal deadlines to stop unwanted contractual traps.",
                "Includes pre-formatted formal non-renewal notices and pricing renegotiation counter-letters."
            ),
            badge = "Deliverable: Expiration Calendar + Notices",
            specTag = "Auto-Renewal Defense • SOW Tracker",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "B2B-SEO-04",
            page = 8,
            cardIndex = 3,
            realmId = "midgard",
            runePill = "ᛗ MIDGARD • UPTIME PULSE",
            meaning = "Bifröst Pulse • 24/7 Synthetic Monitoring & Link Health",
            title = "B2B Synthetic Uptime & Link Health",
            price = "$85–$120",
            priceSub = "Per Month",
            attrs = listOf("Heartbeat" to "60-Second Synthetic Ping", "Audit" to "Monthly Broken Link Crawl", "Effort" to "Automated Cron Monitor"),
            bullets = listOf(
                "Continuous 24/7 synthetic 60-second ping monitoring across commercial web applications and API endpoints with instant SMS escalation.",
                "Monthly automated deep crawl identifying 404 errors, broken partner links, SSL chain anomalies, and sitemap issues.",
                "Automated monthly executive reliability scorecard detailing uptime percentage, TTFB latency, and DNS resolution speed."
            ),
            badge = "Deliverable: 24/7 Monitoring + Monthly Pulse",
            specTag = "Synthetic Uptime • 404 Crawl • SLA",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        ),
        ServiceItem(
            sku = "B2B-SOP-05",
            page = 8,
            cardIndex = 4,
            realmId = "alfheim",
            runePill = "ᚨ ANSUZ • LIVING RUNBOOK",
            meaning = "Chancellor Desk • SOP Maintenance & Onboarding Desk",
            title = "Quarterly SOP Maintenance & Onboarding Desk",
            price = "$165–$225",
            priceSub = "Per Month",
            attrs = listOf("Cadence" to "Monthly Desk + Quarterly Runbook", "Revisions" to "Up to 2 Docs / Month", "Effort" to "Standardized Template Tuning"),
            bullets = listOf(
                "Ongoing maintenance and version control of operational SOP runbooks, contractor intake forms, and client handoff guides.",
                "Includes up to 2 custom administrative document updates or new fillable PDF forms per billing period with 24h turnaround.",
                "Ensures vital corporate workflows and operational institutional memory remain documented as personnel transition."
            ),
            badge = "Deliverable: Living SOP Vault + Admin Desk",
            specTag = "SOP Maintenance • Contractor Intake",
            isRemoteDeliverable = true,
            requiresLicenseOrCert = false
        )
    )

    val flagshipBundle = BundlePlan(
        sku = "RET-ALL-07",
        title = "The All-Realm Executive Steward Retainer",
        stewardPrice = "$165–$215 / Mo",
        stewardPersonal = "$165 / Month (Personal Tier)",
        stewardBusiness = "$215 / Month (Business Tier)",
        techOnlyPass = "$135–$165 / Month",
        threeRealmLaunch = "$495 Flat",
        threeRealmSavings = "Save $225 on Initial Launch",
        features = listOf(
            "Complete coverage across Web (Midgard), Shield (Asgard), Forms (Vanaheim), and Vault (Niflheim).",
            "Includes up to 3 monthly content updates and continuous DNSSEC / DKIM threat surveillance.",
            "Bi-monthly expense optimization and statement audit with 0-day statement purge guarantee.",
            "Direct priority dispatch line to lead consultant via call or text."
        )
    )

    private val _dynamicServices = mutableStateListOf<ServiceItem>().apply {
        addAll(defaultServices)
    }

    val services: List<ServiceItem> get() = _dynamicServices

    var catalogSourceLabel by mutableStateOf("⚡ Dynamic Local")
    var catalogSourceEndpoint by mutableStateOf("https://raw.githubusercontent.com/kalcola/tyrs-technical-consultation-/main/data/catalog.json")

    fun reloadDefaultServices() {
        _dynamicServices.clear()
        _dynamicServices.addAll(defaultServices)
        catalogSourceLabel = "📦 Local Default"
    }

    fun updateServices(newServices: List<ServiceItem>, sourceLabel: String, endpoint: String) {
        if (newServices.isNotEmpty()) {
            _dynamicServices.clear()
            _dynamicServices.addAll(newServices)
            catalogSourceLabel = sourceLabel
            catalogSourceEndpoint = endpoint
        }
    }

    val corporateB2BBundle = BundlePlan(
        sku = "RET-B2B-08",
        title = "The Corporate Sentry & Infrastructure Retainer",
        stewardPrice = "$345–$495 / Mo",
        stewardPersonal = "$345 / Month (Tier 1: <10 seats)",
        stewardBusiness = "$495 / Month (Tier 2: Up to 50 seats)",
        techOnlyPass = "$245 / Month (Cyber + Uptime Only)",
        threeRealmLaunch = "$795 Flat",
        threeRealmSavings = "Save $350 on B2B Setup",
        features = listOf(
            "Monthly external cyber reconnaissance, open port scan, and dark web credential exposure check.",
            "Continuous 24/7 synthetic website uptime monitoring with instant outage escalation.",
            "Monthly SaaS & cloud spend drift audit finding unassigned licenses and idle cloud resources.",
            "Vendor contract renewal tracking preventing accidental evergreen auto-renewals.",
            "Direct priority escalation channel for technical emergencies and vendor dispute drafting."
        )
    )

    val workflowSteps = listOf(
        WorkflowStep(
            step = 1,
            title = "Select Your Realm & SKU",
            realm = "Midgard / All Realms",
            description = "Choose any flat-rate standalone service or care plan with transparent pricing and zero hidden fees.",
            actionLabel = "Browse Catalog"
        ),
        WorkflowStep(
            step = 2,
            title = "Zero-Custody Intake",
            realm = "Asgard Shield",
            description = "Direct telephone or encrypted email dispatch. Zero credentials stored on foreign servers; raw statements purged 0-day.",
            actionLabel = "Review Privacy Policy"
        ),
        WorkflowStep(
            step = 3,
            title = "100% Master Ownership Handover",
            realm = "Vanaheim Harvest",
            description = "Delivered within 24–48 hours: clean source code, unencrypted master PDFs, and custom spreadsheets with $0 lock-in.",
            actionLabel = "Direct Dispatch"
        )
    )

    val presetPackages = listOf(
        PresetPackage(
            id = "pkg-starter",
            title = "Solo Operator Remote Starter",
            runeBadge = "ᛗ ᛉ ᚷ Starter",
            description = "Custom Website + Locked Email Identity + 3-Doc E-Sign Suite",
            serviceSkus = listOf("SVC-WEB-01", "SVC-DOM-02", "SVC-OPS-05"),
            bundledPrice = "$495",
            savings = "Save $100 (Regular $595)",
            turnaround = "48h Remote Turnaround"
        ),
        PresetPackage(
            id = "pkg-ops-armor",
            title = "Zero-Hurdle Operations & Security Pack",
            runeBadge = "ᛏ ᛉ ᛟ Armor",
            description = "SOP Runbook + Workstation Hardening + 3-2-1 Backup Suite",
            serviceSkus = listOf("SVC-SOP-11", "SVC-SEC-12", "SVC-BCK-14"),
            bundledPrice = "$395",
            savings = "Save $75 (Regular $470)",
            turnaround = "48h Remote Turnaround"
        ),
        PresetPackage(
            id = "pkg-freelancer-tax",
            title = "Sovereign Freelancer & Tax-Shield Pack",
            runeBadge = "ᚠ ᚲ ᚷ Sovereign",
            description = "1099 Schedule C Ledger + Executive Proposal Kit + Contract Suite",
            serviceSkus = listOf("SVC-TAX-13", "SVC-DCK-15", "SVC-OPS-05"),
            bundledPrice = "$420",
            savings = "Save $70 (Regular $490)",
            turnaround = "48h Remote Turnaround"
        ),
        PresetPackage(
            id = "pkg-b2b-procurement",
            title = "B2B Vendor Onboarding & Procurement Suite",
            runeBadge = "ᚷ ᛉ ᛏ B2B Suite",
            description = "Master Services Agreement (MSA) + SOW + Wire Fraud SOP + Security Pack",
            serviceSkus = listOf("SVC-B2B-16", "SVC-B2B-17", "SVC-OPS-05"),
            bundledPrice = "$585",
            savings = "Save $140 (Regular $725)",
            turnaround = "48h Turnaround"
        ),
        PresetPackage(
            id = "pkg-b2b-monthly-sentry",
            title = "B2B Monthly Cyber & Spend Sentry Retainer",
            runeBadge = "ᛉ ᚠ ᚦ Retainer",
            description = "Monthly Cyber Hygiene + Cloud Spend Watchdog + Vendor Contract Sentinel",
            serviceSkus = listOf("B2B-SEC-01", "B2B-SLA-02", "B2B-LGL-03"),
            bundledPrice = "$325 / Mo",
            savings = "Save $40 / Month ($365 Regular)",
            turnaround = "Monthly Recurring"
        )
    )
}

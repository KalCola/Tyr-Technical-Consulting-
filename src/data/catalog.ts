export interface RealmInfo {
  id: string;
  number: number;
  rune: string;
  name: string;
  main: string;
  light: string;
  label: string;
  meaning: string;
}

export interface ServiceItem {
  sku: string;
  page: number;
  cardIndex: number;
  realmId: string;
  runePill: string;
  meaning: string;
  title: string;
  price: string;
  priceSub: string;
  priceValue: number;
  isRecurring: boolean;
  attrs: [string, string][];
  bullets: string[];
  badge: string;
  specTag: string;
}

export interface BundlePlan {
  sku: string;
  title: string;
  stewardPrice: string;
  stewardPersonal: string;
  stewardBusiness: string;
  techOnlyPass: string;
  threeRealmLaunch: string;
  threeRealmSavings: string;
  features: string[];
}

export interface WorkflowStep {
  step: number;
  title: string;
  realm: string;
  description: string;
  actionLabel: string;
}

export const ORGANIZATION = {
  name: "TYR Technical Consulting",
  legalName: "CALEB TR HOLCOMB d/b/a TYR",
  phone: "(659) 257-8043",
  phoneRaw: "+16592578043",
  email: "tyr@tyrprotocol.pro",
  naics: ["541512 (Computer Systems)", "541611 (Admin Consulting)"],
  region: "Northwest Alabama (Lamar & Marion) & Nationwide",
  priceRange: "$65 – $495 Flat",
  website: "https://tyrprotocol.pro"
};

export const REALMS: Record<string, RealmInfo> = {
  midgard: {
    id: "midgard",
    number: 1,
    rune: "ᛗ",
    name: "Midgard",
    main: "#dc2626",
    light: "#f87171",
    label: "Websites ($245)",
    meaning: "Realm of Visible Presence • Your Public Front Door"
  },
  asgard: {
    id: "asgard",
    number: 2,
    rune: "ᛉ",
    name: "Asgard",
    main: "#d97706",
    light: "#fbbf24",
    label: "Email Shield ($165)",
    meaning: "Fortified Citadel • Domain & Email Identity Shield"
  },
  vanaheim: {
    id: "vanaheim",
    number: 3,
    rune: "ᚱ",
    name: "Vanaheim",
    main: "#059669",
    light: "#34d399",
    label: "QR & Forms ($150)",
    meaning: "Realm of Trade & Harvest • Fast Customer Intake"
  },
  alfheim: {
    id: "alfheim",
    number: 4,
    rune: "ᚨ",
    name: "Alfheim",
    main: "#0284c7",
    light: "#38bdf8",
    label: "Resumes & Admin ($145)",
    meaning: "Realm of Light & Clarity • Stand Out From the Crowd"
  },
  nidavellir: {
    id: "nidavellir",
    number: 5,
    rune: "ᚷ",
    name: "Nidavellir",
    main: "#ea580c",
    light: "#fb923c",
    label: "Contracts ($185)",
    meaning: "Master Forge • Ironclad Written Agreements"
  },
  muspelheim: {
    id: "muspelheim",
    number: 6,
    rune: "ᚲ",
    name: "Muspelheim",
    main: "#e11d48",
    light: "#fb7185",
    label: "Buyer Guide ($150)",
    meaning: "Fire & Torchlight • Vetted Buyer Research"
  },
  svartalfheim: {
    id: "svartalfheim",
    number: 7,
    rune: "ᚠ",
    name: "Svartalfheim",
    main: "#9333ea",
    light: "#c084fc",
    label: "Expense Opt ($85+)",
    meaning: "Hidden Treasury • Private Offline Wealth Hub"
  },
  niflheim: {
    id: "niflheim",
    number: 8,
    rune: "ᛟ",
    name: "Niflheim",
    main: "#0d9488",
    light: "#2dd4bf",
    label: "Home Vault ($155)",
    meaning: "Deep Preservation • Cold-Storage Asset Vault"
  },
  jotunheim: {
    id: "jotunheim",
    number: 9,
    rune: "ᚦ",
    name: "Jotunheim",
    main: "#4f46e5",
    light: "#818cf8",
    label: "Bill Watch ($75+)",
    meaning: "Giant-Slaying • Defeat Unfair Corporate Bills"
  }
};

export const SERVICES: ServiceItem[] = [
  // Page 1
  {
    sku: "SVC-WEB-01",
    page: 1,
    cardIndex: 0,
    realmId: "midgard",
    runePill: "ᛗ MIDGARD",
    meaning: "Realm of Visible Presence • Your Public Front Door",
    title: "Custom Websites & Portfolios",
    price: "$245",
    priceSub: "One-Time Build",
    priceValue: 245,
    isRecurring: false,
    attrs: [["Norse Realm", "ᛗ Midgard"], ["Hosting Fee", "$0 / Month"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Modern, responsive single-page or multi-section design tailored for local businesses and contractors.",
      "Zero mandatory monthly hosting bills — hosted on free high-speed global infrastructure.",
      "Includes master code repository delivery with $0 platform lock-in."
    ],
    badge: "Deliverable: Live Site + Master Code",
    specTag: "HTML5 • QR • Lead Sheet"
  },
  {
    sku: "SVC-DOM-02",
    page: 1,
    cardIndex: 1,
    realmId: "asgard",
    runePill: "ᛉ ASGARD",
    meaning: "Fortified Citadel • Domain & Email Identity Shield",
    title: "Custom Domain & Protected Email",
    price: "$165",
    priceSub: "One-Time Setup",
    priceValue: 165,
    isRecurring: false,
    attrs: [["Norse Realm", "ᛉ Asgard"], ["Protection", "Anti-Spoof Lock"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Custom domain registration and DNS record provisioning (Cloudflare / Namecheap).",
      "Full anti-spoof email configuration: SPF, DKIM, DMARC, and DNSSEC protection.",
      "Prevents phishing impersonation and stops messages from landing in spam folders."
    ],
    badge: "Deliverable: Domain + Locked Email",
    specTag: "SPF • DKIM • DMARC • DNSSEC"
  },
  {
    sku: "SVC-KIT-03",
    page: 1,
    cardIndex: 2,
    realmId: "vanaheim",
    runePill: "ᚱ VANAHEIM",
    meaning: "Realm of Trade & Harvest • Fast Customer Intake",
    title: "QR Menus & Online Form Kits",
    price: "$150",
    priceSub: "One-Time Kit",
    priceValue: 150,
    isRecurring: false,
    attrs: [["Norse Realm", "ᚱ Vanaheim"], ["Monthly Fee", "$0 / Month"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Durable vector QR code standees and table tent print-ready PDFs.",
      "Mobile customer intake form linked directly to Google Sheets with real-time email alerts.",
      "No recurring software subscriptions or third-party menu portal fees."
    ],
    badge: "Deliverable: QR Standee + Form + Sheet",
    specTag: "Print PDF • Auto-Sheet"
  },

  // Page 2
  {
    sku: "SVC-CAR-04",
    page: 2,
    cardIndex: 0,
    realmId: "alfheim",
    runePill: "ᚨ ALFHEIM",
    meaning: "Realm of Light & Clarity • Stand Out From the Crowd",
    title: "Resume & Contractor Bid Overhaul",
    price: "$145",
    priceSub: "One-Time Kit",
    priceValue: 145,
    isRecurring: false,
    attrs: [["Norse Realm", "ᚨ Alfheim"], ["Formats", "Word + PDF"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Modernized, ATS-compliant executive resume with targeted industry keywords.",
      "Professional contractor bid template for trade quotes and estimates.",
      "Delivered in unlocked Microsoft Word (.docx) and high-res vector PDF."
    ],
    badge: "Deliverable: Resume + Cover + Bid PDF",
    specTag: "ATS-Ready • Word + PDF"
  },
  {
    sku: "SVC-OPS-05",
    page: 2,
    cardIndex: 1,
    realmId: "nidavellir",
    runePill: "ᚷ NIDAVELLIR",
    meaning: "Master Forge • Ironclad Written Agreements",
    title: "3-Doc Fillable E-Sign Contract Kit",
    price: "$185",
    priceSub: "3-Doc Suite",
    priceValue: 185,
    isRecurring: false,
    attrs: [["Norse Realm", "ᚷ Nidavellir"], ["Signing", "Phone or PC"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Three custom interactive fillable AcroForm PDFs: Work Order, Change Order, and Final Sign-Off.",
      "Legally binding e-signature fields compliant with ESIGN and UETA standards.",
      "Reusable forever on mobile phones, tablets, or desktop PCs without DocuSign fees."
    ],
    badge: "Deliverable: 3 Interactive Fillable PDFs",
    specTag: "AcroForm • ESIGN/UETA"
  },
  {
    sku: "SVC-BUY-06",
    page: 2,
    cardIndex: 2,
    realmId: "muspelheim",
    runePill: "ᚲ MUSPELHEIM",
    meaning: "Fire & Torchlight • Vetted Buyer Research",
    title: "Vehicle & Gear Buyer's Dossier",
    price: "$150",
    priceSub: "Research Dossier",
    priceValue: 150,
    isRecurring: false,
    attrs: [["Norse Realm", "ᚲ Muspelheim"], ["Comparison", "Up to 3 Models"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Comprehensive buyer's dossier comparing up to 3 candidate vehicles or heavy equipment pieces.",
      "Audit of common failure points, Technical Service Bulletins (TSBs), and NHTSA recalls.",
      "Fair value pricing matrix and point-by-point in-person inspection checklist."
    ],
    badge: "Deliverable: 3-Option Comparison + Checklist",
    specTag: "NHTSA • TSB • Fair Value"
  },

  // Page 3
  {
    sku: "SVC-CMD-07",
    page: 3,
    cardIndex: 0,
    realmId: "svartalfheim",
    runePill: "ᚠ SVARTALFHEIM",
    meaning: "Hidden Treasury • Private Offline Wealth Hub",
    title: "Home Budget & Business Tracker",
    price: "$195",
    priceSub: "Custom Workbook",
    priceValue: 195,
    isRecurring: false,
    attrs: [["Norse Realm", "ᚠ Svartalfheim"], ["Privacy", "100% Offline"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Self-contained, automated financial tracking workbook built in Excel and Google Sheets.",
      "Categorized income/expense ledgers and Schedule C tax prep summaries.",
      "100% offline privacy — zero cloud bank credential linking or Plaid telemetry."
    ],
    badge: "Deliverable: Custom Excel + Sheets Hub",
    specTag: ".XLSX • Schedule C Ready"
  },
  {
    sku: "SVC-VLT-08",
    page: 3,
    cardIndex: 1,
    realmId: "niflheim",
    runePill: "ᛟ NIFLHEIM",
    meaning: "Deep Preservation • Cold-Storage Asset Vault",
    title: "Home Insurance & Legacy Vault",
    price: "$155",
    priceSub: "Vault & Binder Kit",
    priceValue: 155,
    isRecurring: false,
    attrs: [["Norse Realm", "ᛟ Niflheim"], ["Purpose", "Storm & Theft"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Structured property inventory ledger formatted for rapid storm or theft insurance claims.",
      "Emergency disaster grab-bag PDF documenting serial numbers, receipts, and policy limits.",
      "Standardized proof-of-loss file structure ready for physical flash drive or safe binder."
    ],
    badge: "Deliverable: Claim Ledger + Emergency PDF",
    specTag: "Proof-of-Loss • Offline"
  },
  {
    sku: "SVC-AUD-09",
    page: 3,
    cardIndex: 2,
    realmId: "jotunheim",
    runePill: "ᚦ JOTUNHEIM",
    meaning: "Giant-Slaying • Defeat Unfair Corporate Bills",
    title: "Billing, Warranty & ISP Dispute Kit",
    price: "$175",
    priceSub: "Dispute Dossier",
    priceValue: 175,
    isRecurring: false,
    attrs: [["Norse Realm", "ᚦ Jotunheim"], ["Goal", "Bill Credits"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Formal dispute packet targeting utility billing overcharges, telecom rate hikes, or denied warranty claims.",
      "Constructed with statutory legal citations under FCC, UCC, and Fair Credit Billing regulations.",
      "Complete evidence ledger, demand letter PDF, and step-by-step phone negotiation script."
    ],
    badge: "Deliverable: Formal Evidence Dossier PDF",
    specTag: "FCC • UCC • FCRA • Exhibits"
  },

  // Page 6: Pure Remote Deliverables (No License or Certification Requirements)
  {
    sku: "SVC-SOP-11",
    page: 6,
    cardIndex: 0,
    realmId: "nidavellir",
    runePill: "ᛏ TIWAZ • OPERATIONAL PLAYBOOK",
    meaning: "Sovereign Operations • Standard Operating Procedure (SOP) Vault",
    title: "SOP & Knowledge Base Blueprint",
    price: "$175",
    priceSub: "One-Time Build",
    priceValue: 175,
    isRecurring: false,
    attrs: [["Remote Deliverable", "100% Digital Turnkey"], ["Prerequisites", "Zero Licenses / Certs"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Turnkey Markdown, Obsidian, and Notion knowledge base for solopreneurs, remote teams, and trade contractors.",
      "Standardized core runbooks: client onboarding, emergency escalation tree, and service delivery checklists.",
      "Delivered in open Markdown (.md) and PDF formats with $0 subscription lock-in or recurring SaaS fees."
    ],
    badge: "Deliverable: Markdown Vault + PDF Runbook",
    specTag: "Obsidian/Notion • SOP • 0 License"
  },
  {
    sku: "SVC-SEC-12",
    page: 6,
    cardIndex: 1,
    realmId: "asgard",
    runePill: "ᛉ ALGIZ • WORKSTATION FORTRESS",
    meaning: "Sanctuary Citadel • Zero-Trust Remote Workstation Hardening",
    title: "Remote Workstation Hardening & DNS Profile",
    price: "$135",
    priceSub: "One-Time Kit",
    priceValue: 135,
    isRecurring: false,
    attrs: [["Remote Deliverable", "100% Digital Profiles"], ["Prerequisites", "Zero IT Certs Required"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Ready-to-install DNS-over-HTTPS (DoH) / DNSSEC configuration profiles routing through encrypted Quad9 9.9.9.9.",
      "Browser compartmentalization, anti-telemetry guides, and passwordless FIDO2 passkey onboarding walkthrough.",
      "Shields home office workstations against phishing, drive-by malware, and credential stuffing without costly enterprise MDMs."
    ],
    badge: "Deliverable: DoH MobileConfig + Hardening Guide",
    specTag: "Quad9 • DoH • Passkeys • 0 Cert"
  },
  {
    sku: "SVC-TAX-13",
    page: 6,
    cardIndex: 2,
    realmId: "svartalfheim",
    runePill: "ᚠ FEHU • SCHEDULE C SHIELD",
    meaning: "Flowing Treasury • 1099 Expense & Tax-Shield Ledger Hub",
    title: "1099 Contractor Schedule C Ledger",
    price: "$165",
    priceSub: "Custom Workbook",
    priceValue: 165,
    isRecurring: false,
    attrs: [["Remote Deliverable", "100% Offline Ledger"], ["Prerequisites", "No CPA License Needed"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Pre-built automated Excel & Google Sheets workbook tailored for independent 1099 contractors and sole proprietors.",
      "Auto-calculating Schedule C business expense deductions, mileage log, and quarterly estimated tax reserve formulas.",
      "Includes fillable W-9 pass-through billing templates with complete privacy — zero bank account linking."
    ],
    badge: "Deliverable: Custom Workbook + W-9 Master",
    specTag: ".XLSX • Schedule C • Tax Reserve"
  },
  {
    sku: "SVC-BCK-14",
    page: 6,
    cardIndex: 3,
    realmId: "niflheim",
    runePill: "ᛟ OTHALA • 3-2-1 PRESERVATION",
    meaning: "Ancestral Heritage • Cold-Storage Disaster Recovery Runbook",
    title: "Cold-Storage 3-2-1 Backup Runbook",
    price: "$160",
    priceSub: "Automation Kit",
    priceValue: 160,
    isRecurring: false,
    attrs: [["Remote Deliverable", "100% Scripted Playbook"], ["Prerequisites", "Zero Backup Certs Needed"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Turnkey 3-2-1 backup architecture: 3 copies of business data, on 2 distinct media types, with 1 offsite encrypted archive.",
      "Automated copy-on-write sync scripts (PowerShell & Bash) for external drives and zero-knowledge encrypted cloud remotes (rclone).",
      "Includes periodic restore verification drill checklist to guarantee uncorrupted asset recovery after hardware crashes."
    ],
    badge: "Deliverable: Automation Scripts + DR Manual",
    specTag: "3-2-1 Backup • Rclone • Zero-SaaS"
  },
  {
    sku: "SVC-DCK-15",
    page: 6,
    cardIndex: 4,
    realmId: "muspelheim",
    runePill: "ᚲ KAUNAN • HIGH-IMPACT PROPOSAL",
    meaning: "Torch of Insight • Executive High-Conversion Proposal & Pitch Deck",
    title: "Executive Proposal & Pitch Deck Kit",
    price: "$140",
    priceSub: "Turnkey Templates",
    priceValue: 140,
    isRecurring: false,
    attrs: [["Remote Deliverable", "100% Editable Templates"], ["Prerequisites", "Zero Design Skills Needed"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "10-slide high-impact typographic master pitch deck formatted for PowerPoint (.pptx), Google Slides, and vector PDF.",
      "Formal client proposal and quotation document template with embedded ROI calculators and clear scope boundaries.",
      "Engineered for trade contractors and consultants to quote projects with corporate prestige and eliminate scope creep."
    ],
    badge: "Deliverable: PPTX Deck + Proposal PDF",
    specTag: "PowerPoint • Google Slides • PDF"
  },

  // Page 4: Subscriptions 1
  {
    sku: "SUB-WEB-01",
    page: 4,
    cardIndex: 0,
    realmId: "midgard",
    runePill: "ᛗ MIDGARD + ᚱ VANAHEIM",
    meaning: "Storefront Guardian • Active Website Upkeep",
    title: "Website & QR Menu Care Plan",
    price: "$55–$75",
    priceSub: "Per Month",
    priceValue: 65,
    isRecurring: true,
    attrs: [["Personal Tier", "$55 / Month"], ["Business Tier", "$75 / Month"], ["Edits Included", "Up to 2 / Mo"]],
    bullets: [
      "Ongoing maintenance for websites and QR digital menus.",
      "Includes 2 text/content revisions per month, link verification, and intake form checks.",
      "Emergency hotline support for urgent updates or holiday hours changes."
    ],
    badge: "2 Updates/Mo + Form Check",
    specTag: "NAICS 541512 • Web Care"
  },
  {
    sku: "SUB-DOM-02",
    page: 4,
    cardIndex: 1,
    realmId: "asgard",
    runePill: "ᛉ ASGARD WATCHTOWER",
    meaning: "Bifröst Shield • Continuous Domain & Inbox Watch",
    title: "Domain & Email Shield Plan",
    price: "$65–$85",
    priceSub: "Per Month",
    priceValue: 75,
    isRecurring: true,
    attrs: [["Personal Tier", "$65 / Month"], ["Business Tier", "$85 / Month"], ["Protection", "Monthly Scan"]],
    bullets: [
      "Continuous automated monitoring of domain expiration, SSL certificates, and DNS records.",
      "Monthly deliverability audits checking SPF/DKIM integrity and email reputation.",
      "Proactive breach alerts if organizational email addresses appear in credential leaks."
    ],
    badge: "Deliverability & Breach Scan",
    specTag: "NAICS 541512 • DNS Shield"
  },
  {
    sku: "SUB-OPS-03",
    page: 4,
    cardIndex: 2,
    realmId: "niflheim",
    runePill: "ᛟ NIFLHEIM VAULT",
    meaning: "Deep Vault Keeper • Monthly Edits & Recovery",
    title: "Document & Vault Caretaker Plan",
    price: "$75–$95",
    priceSub: "Per Month",
    priceValue: 85,
    isRecurring: true,
    attrs: [["Personal Tier", "$75 / Month"], ["Business Tier", "$95 / Month"], ["Data Retained", "Blank Masters"]],
    bullets: [
      "Caretaker support for operational fillable forms, inventory records, and contract templates.",
      "Includes 1 template modification per month and permanent cold-storage backup of blank master files.",
      "Instant disaster re-issuance if original files are lost to device crashes."
    ],
    badge: "1 Doc Update/Mo + Recovery",
    specTag: "7-Yr Blank Master Vault"
  },

  // Page 5: Subscriptions 2
  {
    sku: "SUB-FIN-04",
    page: 5,
    cardIndex: 0,
    realmId: "svartalfheim",
    runePill: "ᚠ SVARTALFHEIM • FEHU",
    meaning: "Treasury Audit • Done-For-You Statement Review",
    title: "Expense & Subscription Optimizer",
    price: "$85–$115",
    priceSub: "Per Month",
    priceValue: 95,
    isRecurring: true,
    attrs: [["Personal / Biz", "$85 / $115 Mo"], ["Raw Statements", "Purged (0-Day)"], ["Retained File", "Ledger + Report"]],
    bullets: [
      "Done-for-you monthly statement audit identifying unwanted recurring charges and creeping service fees.",
      "Delivered as an actionable executive savings summary and cancellation checklist.",
      "Strict Zero-Custody Guarantee: All client banking statements destroyed within 24 hours of report delivery."
    ],
    badge: "Deliverable: Monthly Savings & Cut Report",
    specTag: "NAICS 541611 • 0 Raw Data Kept"
  },
  {
    sku: "SUB-BIL-05",
    page: 5,
    cardIndex: 1,
    realmId: "jotunheim",
    runePill: "ᚦ JOTUNHEIM • THURISAZ",
    meaning: "Giant-Slayer Watch • Stop Creeping Vendor Rates",
    title: "Utility & Vendor Bill Watchdog",
    price: "$75–$105",
    priceSub: "Per Month",
    priceValue: 85,
    isRecurring: true,
    attrs: [["Personal / Biz", "$75 / $105 Mo"], ["Source Bills", "Purged (0-Day)"], ["Retained File", "Ledger + Report"]],
    bullets: [
      "Ongoing rate monitoring across telecom, internet, insurance premiums, and utility invoices.",
      "Detects post-promotional fee hikes and provides scripted negotiation points to roll back rates.",
      "Zero raw statement retention — ephemeral audits ensuring complete financial confidentiality."
    ],
    badge: "Deliverable: Rate-Defense Report + Scripts",
    specTag: "NAICS 541611 • Ephemeral Audit"
  },
  {
    sku: "SUB-ADM-06",
    page: 5,
    cardIndex: 2,
    realmId: "alfheim",
    runePill: "ᚨ ALFHEIM + ᚷ FORGE",
    meaning: "Back-Office Steward • On-Demand Quotes & Forms",
    title: "Back-Office Admin & Quote Concierge",
    price: "$80–$110",
    priceSub: "Per Month",
    priceValue: 95,
    isRecurring: true,
    attrs: [["Personal / Biz", "$80 / $110 Mo"], ["Tasks Included", "Up to 3 / Mo"], ["Retained File", "Ledger + Report"]],
    bullets: [
      "On-demand virtual back-office assistant for generating contractor bid estimates, invoices, and PDF forms.",
      "Includes up to 3 custom administrative deliverables per billing cycle with fast 24h turnaround.",
      "Empowers solo operators to look like high-powered corporate enterprises without hiring full-time staff."
    ],
    badge: "Deliverable: 3 Admin Builds/Mo + Report",
    specTag: "NAICS 541611 • 0 Client PII Kept"
  },
  {
    sku: "SVC-MIG-01",
    page: 4,
    cardIndex: 4,
    realmId: "midgard",
    runePill: "ᛗ Midgard",
    meaning: "Zero-Downtime Migration & Registrar Liberation",
    title: "Domain, DNS & Server Cutover Migration",
    price: "$250",
    priceSub: "Flat Rate • Zero Downtime Handover",
    priceValue: 250,
    isRecurring: false,
    attrs: [["Propagation", "TTL Pre-Drop"], ["Registrar", "Client Kept"], ["Downtime", "0.00 Seconds"]],
    bullets: [
      "Full migration of domains, DNS records, static assets, and subdomains from legacy hosts (GoDaddy, Wix, Bluehost).",
      "Pre-staged DNS records with minimal TTL to ensure zero downtime or lost customer emails during handover.",
      "Complete credential handover back to client ownership with zero backdoor retention."
    ],
    badge: "Deliverable: Zero-Downtime DNS Cutover + Registry Keys",
    specTag: "RFC 1035 • 100% Client Domain Custody"
  },
  {
    sku: "SVC-SEC-01",
    page: 1,
    cardIndex: 4,
    realmId: "asgard",
    runePill: "ᛉ Asgard",
    meaning: "Email Anti-Spoof Fortress & Reputational Armor",
    title: "DMARC, DKIM & SPF Cryptographic Hardening Audit",
    price: "$300",
    priceSub: "Flat Rate • Enforcement Mode (p=reject)",
    priceValue: 300,
    isRecurring: false,
    attrs: [["Policy", "p=reject Active"], ["DKIM Keys", "2048-bit RSA"], ["Inbox Rate", "99.8% Verified"]],
    bullets: [
      "End-to-end cryptographic email authentication stopping scammers and bad actors from spoofing your business name.",
      "Full alignment of SPF, DKIM 2048-bit selectors, and DMARC enforcement policy with daily aggregate reporting.",
      "Guarantees quotes, invoices, and executive emails land in the primary inbox, never spam folders."
    ],
    badge: "Deliverable: DMARC p=reject Audit + DNS Hardening",
    specTag: "RFC 7489 • Zero-Spoof Guarantee"
  },
  {
    sku: "SVC-OPT-01",
    page: 3,
    cardIndex: 4,
    realmId: "niflheim",
    runePill: "ᚠ Niflheim",
    meaning: "Sovereign Cloud & SaaS Cost Elimination",
    title: "Hosting, Cloud & SaaS Statement Expense Audit",
    price: "$400",
    priceSub: "Flat Rate • Find $1k+ or 100% Refund",
    priceValue: 400,
    isRecurring: false,
    attrs: [["Target Savings", "15%–40% Recurring"], ["Purge Window", "24-Hour Purge"], ["Deliverable", "Redline Dossier"]],
    bullets: [
      "Forensic statement-by-statement review of your AWS, GCP, Cloudflare, Adobe, Google Workspace, and SaaS invoices.",
      "Identifies zombie subscriptions, forgotten instances, unattached EBS volumes, and predatory auto-renewals.",
      "Includes customized negotiation script and migration roadmap to slash overhead with $0 ongoing consulting fees."
    ],
    badge: "Deliverable: Redline Savings Dossier + Action Scripts",
    specTag: "NAICS 541611 • Zero Statement Retention"
  },

  // Page 7: B2B Procurement & Corporate Governance Suite
  {
    sku: "SVC-B2B-16",
    page: 7,
    cardIndex: 0,
    realmId: "nidavellir",
    runePill: "ᚷ GEBO • B2B PROCUREMENT",
    meaning: "Master Forge • B2B Vendor Onboarding & Master MSA Suite",
    title: "B2B Vendor Onboarding & Master MSA Suite",
    price: "$295",
    priceSub: "Turnkey Suite",
    priceValue: 295,
    isRecurring: false,
    attrs: [["Scope", "Full Vendor Pack"], ["Format", "Fillable PDF + DOCX"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Complete corporate vendor onboarding suite: Master Services Agreement (MSA), SOW template, Mutual NDA, and W-9 pass-through billing ledger.",
      "Interactive fillable AcroForm PDFs with legally binding e-signature fields and audit trail certificate blocks.",
      "Eliminates $2,000+ attorney drafting retainers while establishing ironclad scope boundaries, payment terms, and liability limits."
    ],
    badge: "Deliverable: 4-Doc MSA & Procurement Suite",
    specTag: "MSA • SOW • NDA • Fillable PDF"
  },
  {
    sku: "SVC-B2B-17",
    page: 7,
    cardIndex: 1,
    realmId: "asgard",
    runePill: "ᛉ ALGIZ • ANTI-FRAUD CITADEL",
    meaning: "Fortified Citadel • Corporate Anti-Phishing & Wire-Fraud Protocol",
    title: "Corporate Anti-Phishing & Wire Protocol",
    price: "$245",
    priceSub: "Turnkey Governance",
    priceValue: 245,
    isRecurring: false,
    attrs: [["Security Model", "Zero-Trust Protocol"], ["Target", "Staff & Execs"], ["Turnaround", "24–48 Hours"]],
    bullets: [
      "Battle-tested employee anti-fraud protocol: verbal out-of-band verification SOP for wire transfers and vendor ACH changes.",
      "Executive mailbox anti-spoofing filter rules, shared inbox access standards, and staff security response runbook.",
      "Stops Business Email Compromise (BEC) and invoice diversion schemes that siphon millions from commercial firms."
    ],
    badge: "Deliverable: Anti-Fraud Runbook + Staff SOP",
    specTag: "BEC Defense • Wire Security • Staff SOP"
  },

  // Page 8: Monthly Low-Effort B2B Retainers (Automated / High-Margin)
  {
    sku: "B2B-SEC-01",
    page: 8,
    cardIndex: 0,
    realmId: "asgard",
    runePill: "ᛉ ASGARD WATCHTOWER",
    meaning: "Perimeter Sentinel • Automated Cyber Hygiene & Leak Scan",
    title: "B2B Monthly Cyber Hygiene Retainer",
    price: "$125–$175",
    priceSub: "Per Month",
    priceValue: 150,
    isRecurring: true,
    attrs: [["Cadence", "Monthly Automated Scan"], ["Effort", "Automated Scripted Report"], ["Client Value", "Executive Assurance"]],
    bullets: [
      "Monthly external perimeter scan detecting open exposed ports (RDP, SSH, DB), insecure services, and certificate expirations.",
      "Dark web credential exposure scan across corporate domains and key executive email addresses.",
      "Delivered as a crisp 1-page executive scorecard with pass/fail grades and prioritized remediation steps."
    ],
    badge: "Deliverable: Monthly 1-Page Risk Scorecard",
    specTag: "External Recon • SSL • Dark Web Check"
  },
  {
    sku: "B2B-SLA-02",
    page: 8,
    cardIndex: 1,
    realmId: "svartalfheim",
    runePill: "ᚠ FEHU • CLOUD AUDIT",
    meaning: "Treasury Sentry • SaaS & Cloud Spend Drift Watchdog",
    title: "Cloud & SaaS Spend Drift Watchdog",
    price: "$145–$195",
    priceSub: "Per Month",
    priceValue: 170,
    isRecurring: true,
    attrs: [["Cadence", "Monthly 30-Min Audit"], ["Avg Savings", "$350–$1,200 / Mo"], ["Access Required", "Read-Only Billing"]],
    bullets: [
      "Monthly forensic review of AWS, GCP, Azure, or SaaS billing (Google Workspace, Slack, Zoom, HubSpot, Adobe).",
      "Automatically flags orphaned cloud storage volumes, unassigned licenses, abandoned elastic IPs, and rate creeps.",
      "Provides turnkey cancellation and rightsizing scripts that consistently yield 3x–10x ROI on the retainer cost."
    ],
    badge: "Deliverable: Monthly Cloud Savings Report",
    specTag: "AWS/GCP • SaaS Seats • Drift Audit"
  },
  {
    sku: "B2B-LGL-03",
    page: 8,
    cardIndex: 2,
    realmId: "jotunheim",
    runePill: "ᚦ THURISAZ • CONTRACT SHIELD",
    meaning: "Giant-Slaying Defense • Vendor Contract Renewal Sentinel",
    title: "Vendor Contract Renewal Sentinel",
    price: "$95–$135",
    priceSub: "Per Month",
    priceValue: 115,
    isRecurring: true,
    attrs: [["Alert Window", "30 / 60 / 90 Days"], ["Capacity", "Up to 25 Contracts"], ["Effort", "Automated Tracker"]],
    bullets: [
      "Proactive centralized calendar monitoring for commercial vendor agreements, equipment leases, and enterprise software.",
      "Issues automated 90, 60, and 30-day alerts prior to evergreen auto-renewal deadlines to stop unwanted contractual traps.",
      "Includes pre-formatted formal non-renewal notices and pricing renegotiation counter-letters."
    ],
    badge: "Deliverable: Expiration Calendar + Notices",
    specTag: "Auto-Renewal Defense • SOW Tracker"
  },
  {
    sku: "B2B-SEO-04",
    page: 8,
    cardIndex: 3,
    realmId: "midgard",
    runePill: "ᛗ MIDGARD • UPTIME PULSE",
    meaning: "Bifröst Pulse • 24/7 Synthetic Monitoring & Link Health",
    title: "B2B Synthetic Uptime & Link Health",
    price: "$85–$120",
    priceSub: "Per Month",
    priceValue: 100,
    isRecurring: true,
    attrs: [["Heartbeat", "60-Second Synthetic Ping"], ["Audit", "Monthly Broken Link Crawl"], ["Effort", "Automated Cron Monitor"]],
    bullets: [
      "Continuous 24/7 synthetic 60-second ping monitoring across commercial web applications and API endpoints with instant SMS escalation.",
      "Monthly automated deep crawl identifying 404 errors, broken partner links, SSL chain anomalies, and sitemap issues.",
      "Automated monthly executive reliability scorecard detailing uptime percentage, TTFB latency, and DNS resolution speed."
    ],
    badge: "Deliverable: 24/7 Monitoring + Monthly Pulse",
    specTag: "Synthetic Uptime • 404 Crawl • SLA"
  },
  {
    sku: "B2B-SOP-05",
    page: 8,
    cardIndex: 4,
    realmId: "alfheim",
    runePill: "ᚨ ANSUZ • LIVING RUNBOOK",
    meaning: "Chancellor Desk • SOP Maintenance & Onboarding Desk",
    title: "Quarterly SOP Maintenance & Onboarding Desk",
    price: "$165–$225",
    priceSub: "Per Month",
    priceValue: 195,
    isRecurring: true,
    attrs: [["Cadence", "Monthly Desk + Quarterly Runbook"], ["Revisions", "Up to 2 Docs / Month"], ["Effort", "Standardized Template Tuning"]],
    bullets: [
      "Ongoing maintenance and version control of operational SOP runbooks, contractor intake forms, and client handoff guides.",
      "Includes up to 2 custom administrative document updates or new fillable PDF forms per billing period with 24h turnaround.",
      "Ensures vital corporate workflows and operational institutional memory remain documented as personnel transition."
    ],
    badge: "Deliverable: Living SOP Vault + Admin Desk",
    specTag: "SOP Maintenance • Contractor Intake"
  }
];

export interface ServiceTier {
  id: string;
  name: string;
  tagline: string;
  badge: string;
  price: string;
  priceSub: string;
  bestFor: string;
  includedSkus: string[];
  keyHighlights: string[];
  agencyComparison: string;
}

export const SERVICE_TIERS: ServiceTier[] = [
  {
    id: "solo_builder",
    name: "Solo Builder Tier",
    tagline: "Essential Public Face & Protected Outreach",
    badge: "Starter",
    price: "$310",
    priceSub: "One-Time Flat Investment",
    bestFor: "Solo tradesmen, local consultants, and independent contractors launching their public presence.",
    includedSkus: ["SVC-WEB-01", "SVC-MAIL-02"],
    keyHighlights: [
      "Custom high-speed website with 100/100 Lighthouse performance",
      "Private custom email address with anti-spoof DNS records",
      "Zero monthly hosting fees via static edge network delivery",
      "Complete master source code and assets handover"
    ],
    agencyComparison: "Typical Agency: $2,500 setup + $150/mo mandatory maintenance."
  },
  {
    id: "operator_pro",
    name: "Operator Pro Tier",
    tagline: "Full Bifröst Commercial Presence & Legal Armor",
    badge: "Most Popular",
    price: "$495",
    priceSub: "One-Time Flat Investment (Save $225)",
    bestFor: "Growing local companies, service businesses, and professional practices demanding high trust.",
    includedSkus: ["SVC-WEB-01", "SVC-MAIL-02", "SVC-FORM-05", "SVC-QR-03"],
    keyHighlights: [
      "Complete flagship website, anti-spoof email, and dynamic QR suite",
      "3-document fillable client contract and change order package",
      "Direct consultation call with lead engineer Caleb TR Holcomb",
      "Day-0 privacy assurance: zero raw statements or PII kept"
    ],
    agencyComparison: "Typical Agency: $5,000+ setup + $400/mo retainer lock-in."
  },
  {
    id: "sovereign_fortress",
    name: "Sovereign Fortress Tier",
    tagline: "Total Technical, Audit & Retainer Stewardship",
    badge: "Executive",
    price: "$495 + $150/mo",
    priceSub: "Launch + Continuous Shield Retainer",
    bestFor: "Established operations needing continuous technical security, monthly expense audits, and priority SLA.",
    includedSkus: ["SVC-WEB-01", "SVC-MAIL-02", "SVC-FORM-05", "SVC-SEC-01", "RET-WEB-01"],
    keyHighlights: [
      "Full Operator Pro launch suite plus DMARC/DKIM cryptographic hardening",
      "24/7 automated website uptime surveillance and DNS watchdog",
      "Bi-monthly expense optimization and software invoice audits",
      "Direct priority phone/text dispatch line with 4-hour SLA"
    ],
    agencyComparison: "Typical Agency: $1,200/mo retainer with 12-month lock-in contract."
  }
];

export interface RetainerPlanInfo {
  id: string;
  sku: string;
  name: string;
  price: string;
  priceValue: number;
  cadence: string;
  badge: string;
  description: string;
  features: string[];
  sla: string;
}

export const RETAINER_PLANS: RetainerPlanInfo[] = [
  {
    id: "web_shield",
    sku: "RET-WEB-01",
    name: "Web & Shield Care",
    price: "$150",
    priceValue: 150,
    cadence: "per month",
    badge: "Continuous Defense",
    description: "Proactive uptime monitoring, DNSSEC surveillance, and up to 2 monthly content/menu updates.",
    features: [
      "24/7 automated synthetic uptime pinging with instant incident alert",
      "Continuous DNS record & SSL certificate expiration monitoring",
      "Up to 2 monthly content, pricing, or staff updates (delivered in 24h)",
      "Zero vendor lock-in — pause or cancel anytime with 1 click"
    ],
    sla: "24-Hour Business Turnaround"
  },
  {
    id: "expense_audit",
    sku: "RET-AUD-02",
    name: "Expense & Audit Watchdog",
    price: "$350",
    priceValue: 350,
    cadence: "per month",
    badge: "Overhead Slasher",
    description: "Monthly reconciliation of recurring SaaS, vendor invoices, and cloud bills to halt billing creep.",
    features: [
      "Monthly forensic statement audit across all recurring software & utilities",
      "Flagging of price increases, shadow subscriptions, and duplicate billings",
      "Pre-drafted vendor renegotiation scripts and cost-cutting directives",
      "Strict Day-0 purge: zero raw financial statements retained on disk"
    ],
    sla: "12-Hour Priority Analysis"
  },
  {
    id: "sovereign_cto",
    sku: "RET-CTO-03",
    name: "Sovereign Fractional CTO",
    price: "$500",
    priceValue: 500,
    cadence: "per month",
    badge: "Full Partnership",
    description: "Executive technical counsel, vendor dispute mediation, software evaluation, and direct private dispatch.",
    features: [
      "Everything in Web Shield and Expense Watchdog included",
      "Direct private phone and text dispatch line to Caleb TR Holcomb",
      "Independent technical vetting for major third-party software bids",
      "Quarterly architecture review and security hardening roadmaps"
    ],
    sla: "4-Hour High-Priority SLA"
  }
];

export const FLAGSHIP_BUNDLE: BundlePlan = {
  sku: "RET-ALL-07",
  title: "The All-Realm Executive Steward Retainer",
  stewardPrice: "$165–$215 / Mo",
  stewardPersonal: "$165 / Month (Personal Tier)",
  stewardBusiness: "$215 / Month (Business Tier)",
  techOnlyPass: "$135–$165 / Month",
  threeRealmLaunch: "$495 Flat",
  threeRealmSavings: "Save $225 on Initial Launch",
  features: [
    "Complete coverage across Web (Midgard), Shield (Asgard), Forms (Vanaheim), and Vault (Niflheim).",
    "Includes up to 3 monthly content updates and continuous DNSSEC / DKIM threat surveillance.",
    "Bi-monthly expense optimization and statement audit with 0-day statement purge guarantee.",
    "Direct priority dispatch line to lead consultant via call or text."
  ]
};

export const CORPORATE_B2B_BUNDLE: BundlePlan = {
  sku: "RET-B2B-08",
  title: "The Corporate Sentry & Infrastructure Retainer",
  stewardPrice: "$345–$495 / Mo",
  stewardPersonal: "$345 / Month (Tier 1: <10 seats)",
  stewardBusiness: "$495 / Month (Tier 2: Up to 50 seats)",
  techOnlyPass: "$245 / Month (Cyber + Uptime Only)",
  threeRealmLaunch: "$795 Flat",
  threeRealmSavings: "Save $350 on B2B Setup",
  features: [
    "Monthly external cyber reconnaissance, open port scan, and dark web credential exposure check.",
    "Continuous 24/7 synthetic website uptime monitoring with instant outage escalation.",
    "Monthly SaaS & cloud spend drift audit finding unassigned licenses and idle cloud resources.",
    "Vendor contract renewal tracking preventing accidental evergreen auto-renewals.",
    "Direct priority escalation channel for technical emergencies and vendor dispute drafting."
  ]
};

export const WORKFLOW_STEPS: WorkflowStep[] = [
  {
    step: 1,
    title: "Select Your Realm & SKU",
    realm: "Midgard / All Realms",
    description: "Choose any flat-rate standalone service or care plan with transparent pricing and zero hidden fees.",
    actionLabel: "Browse All Realms"
  },
  {
    step: 2,
    title: "Zero-Custody Intake",
    realm: "Asgard Shield",
    description: "Direct telephone or encrypted email dispatch. Zero credentials stored on foreign servers; raw statements purged 0-day.",
    actionLabel: "Zero-Custody Privacy Rule"
  },
  {
    step: 3,
    title: "100% Master Ownership Handover",
    realm: "Vanaheim Harvest",
    description: "Delivered within 24–48 hours: clean source code, unencrypted master PDFs, and custom spreadsheets with $0 lock-in.",
    actionLabel: "Direct Dispatch Line"
  }
];

export interface PresetPackage {
  id: string;
  title: string;
  runeBadge: string;
  description: string;
  serviceSkus: string[];
  bundledPrice: string;
  savings: string;
  turnaround: string;
}

export const PRESET_PACKAGES: PresetPackage[] = [
  {
    id: "pkg-starter",
    title: "Solo Operator Remote Starter",
    runeBadge: "ᛗ ᛉ ᚷ Starter",
    description: "Custom Website + Locked Email Identity + 3-Doc E-Sign Suite",
    serviceSkus: ["SVC-WEB-01", "SVC-DOM-02", "SVC-OPS-05"],
    bundledPrice: "$495",
    savings: "Save $100 (Regular $595)",
    turnaround: "48h Remote Turnaround"
  },
  {
    id: "pkg-ops-armor",
    title: "Zero-Hurdle Operations & Security Pack",
    runeBadge: "ᛏ ᛉ ᛟ Armor",
    description: "SOP Runbook + Workstation Hardening + 3-2-1 Backup Suite",
    serviceSkus: ["SVC-SOP-11", "SVC-SEC-12", "SVC-BCK-14"],
    bundledPrice: "$395",
    savings: "Save $75 (Regular $470)",
    turnaround: "48h Remote Turnaround"
  },
  {
    id: "pkg-freelancer-tax",
    title: "Sovereign Freelancer & Tax-Shield Pack",
    runeBadge: "ᚠ ᚲ ᚷ Sovereign",
    description: "1099 Schedule C Ledger + Executive Proposal Kit + Contract Suite",
    serviceSkus: ["SVC-TAX-13", "SVC-DCK-15", "SVC-OPS-05"],
    bundledPrice: "$420",
    savings: "Save $70 (Regular $490)",
    turnaround: "48h Remote Turnaround"
  },
  {
    id: "pkg-b2b-procurement",
    title: "B2B Vendor Onboarding & Procurement Suite",
    runeBadge: "ᚷ ᛉ ᛏ B2B Suite",
    description: "Master Services Agreement (MSA) + SOW + Wire Fraud SOP + Security Pack",
    serviceSkus: ["SVC-B2B-16", "SVC-B2B-17", "SVC-OPS-05"],
    bundledPrice: "$585",
    savings: "Save $140 (Regular $725)",
    turnaround: "48h Turnaround"
  },
  {
    id: "pkg-b2b-monthly-sentry",
    title: "B2B Monthly Cyber & Spend Sentry Retainer",
    runeBadge: "ᛉ ᚠ ᚦ Retainer",
    description: "Monthly Cyber Hygiene + Cloud Spend Watchdog + Vendor Contract Sentinel",
    serviceSkus: ["B2B-SEC-01", "B2B-SLA-02", "B2B-LGL-03"],
    bundledPrice: "$325 / Mo",
    savings: "Save $40 / Month ($365 Regular)",
    turnaround: "Monthly Recurring"
  }
];

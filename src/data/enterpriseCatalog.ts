export interface EnterpriseDeliverablePhase {
  phase: string;
  targetWindow: string;
  deliverables: string[];
}

export interface EnterpriseLineItem {
  code: string;
  description: string;
  billingType: string;
  rateUsd: string;
  totalUsd: string;
}

export interface EnterpriseTrack {
  id: string;
  code: string;
  badge: string;
  title: string;
  roleTitle: string;
  targetClient: string;
  workOrderId: string;
  versionStatus: string;
  rateDescription: string;
  rateUnit: string;
  rateValueMonthly: number;
  pitchNote: string;
  workstationIsolation: string[];
  deliverables30_60_90: EnterpriseDeliverablePhase[];
  lineItems: EnterpriseLineItem[];
  sovereignBoundaries: string[];
  payoutFlowSummary: string;
}

export interface EnterpriseEntityInfo {
  legalEntity: string;
  dba: string;
  ein: string;
  irsNotice: string;
  nameControl: string;
  principalAddress: string;
  email: string;
  phone: string;
  naicsCodes: Array<{ code: string; label: string }>;
  bankingPartner: string;
  achRoutingType: string;
  statutoryAuthority: string;
}

export const ENTERPRISE_ENTITY: EnterpriseEntityInfo = {
  legalEntity: 'CALEB TR HOLCOMB',
  dba: "TYR (Tyr's Technical Consultation)",
  ein: '42-XXX-X843',
  irsNotice: 'CP 575 G (Assigned 08/22/2026)',
  nameControl: 'HOLC',
  principalAddress: 'Northwest Alabama, United States (100% Remote Digital Delivery)',
  email: 'tyr@tyrprotocol.pro',
  phone: '+1 (659) 257-8043',
  naicsCodes: [
    { code: '541512', label: 'Computer Systems Design Services (Primary)' },
    { code: '541511', label: 'Custom Computer Programming Services' },
    { code: '541519', label: 'Other Computer Related Services' },
    { code: '541611', label: 'Administrative & General Management Consulting' }
  ],
  bankingPartner: 'Lili.co Commercial Banking / Sunrise Banks, N.A.',
  achRoutingType: 'Direct ACH & Form W-9 Pass-Through Sole Proprietorship',
  statutoryAuthority:
    'Executed pursuant to 15 U.S.C. § 7001 (Federal ESIGN Act), Ala. Code § 8-1A-1 et seq. (Alabama UETA), 18 U.S.C. § 1836 (DTSA), and IRC § 6501.'
};

export const ENTERPRISE_TRACKS: EnterpriseTrack[] = [
  {
    id: 'ai-eval',
    code: 'WO-2026-1028-02',
    badge: 'ᚱᚢᚾ AI Rubric Audit',
    title: 'Computer Systems Analyst — AI Output & Rubric Evaluation',
    roleTitle: 'Senior Computer Systems Analyst & Rubric Auditor',
    targetClient: 'AI Labs, Frontier Model Teams & Enterprise Human-Data Programs (AuraOne Track)',
    workOrderId: 'DOC-001 • WO-2026-1028-02 / SOW-2026-1028-02',
    versionStatus: 'Rev. 1.0 • Active / Verified Production Scope',
    rateDescription: '$90.00 / hr ($2,250.00 / week batched for 25 hrs)',
    rateUnit: 'per week (async batched)',
    rateValueMonthly: 9000,
    pitchNote:
      'Senior Computer Systems Analyst & Principal Consultant (Tyr\'s Technical Consultation, NAICS 541512) with hands-on production operations across Microsoft 365 Enterprise, Entra ID, Exchange Online, Windows Server 2025, Linux (Fedora/Nobara/Debian/Kali), and ERP/Dynamics-style flat-grid ledger auditing. Daily operator of AI-assisted workflows and Model Context Protocol (MCP) tool pipelines—experienced in applying multi-page rubrics across 25+ row batches, isolating prompt-level defects versus model hallucinations, tagging escalation severity (Concern, Cause, Correction), and writing policy-grounded rationales for model training. US-authorized independent contractor ready for 25–40 hrs/wk async throughput.',
    workstationIsolation: [
      'Encrypted Windows 11 Pro for Workstations & Fedora Linux operations environment',
      'Quad9 9.9.9.9 DoH / DNSSEC hardware-level query protection',
      'Zero-custody processing: 0% client prompt or completion data stored locally post-batch submission',
      'RFC1918 lateral isolation across secure production VLAN'
    ],
    deliverables30_60_90: [
      {
        phase: 'Phase 1: Ingestion & Baseline Calibration',
        targetWindow: 'Days 1–30',
        deliverables: [
          'Master firm playbooks, severity taxonomies (SEV-1 to SEV-4), and escalation rubrics',
          'Attain >=98% inter-rater agreement across M365/Entra, ERP/Dynamics, and OS/network queues',
          'Benchmark initial 25-row calibration batches with deterministic policy justifications'
        ]
      },
      {
        phase: 'Phase 2: High-Throughput Grading & Prompt Defect Triage',
        targetWindow: 'Days 31–60',
        deliverables: [
          'Sustain 25 to 40 hrs/wk async evaluation velocity with zero backlog bottleneck',
          'Flag malformed prompts, missing prerequisite gates, and tool-use failure modes',
          'Author deterministic ground-truth next steps for RLHF / SFT model training pipelines'
        ]
      },
      {
        phase: 'Phase 3: Dispute Adjudication & 25-Row Batch QA',
        targetWindow: 'Days 61–90',
        deliverables: [
          'Serve as senior peer adjudicator for disputed treatments between reviewers using firm guidance',
          'Audit 25-row batches for rubric consistency and report inter-rater drift metrics to program lead',
          'Synthesize model training recommendations and workflow guardrail documentation'
        ]
      }
    ],
    lineItems: [
      {
        code: 'EVAL-STD',
        description: 'Computer Systems Analyst AI Output & Rubric Grading (Weekly 20h Batch)',
        billingType: 'Hourly Batched',
        rateUsd: '$90.00 / hr',
        totalUsd: '$1,800.00 / wk'
      },
      {
        code: 'ADJ-QA',
        description: 'Disputed Treatment Adjudication & 25-Row Batch Drift Audit (Weekly 5h)',
        billingType: 'Hourly Batched',
        rateUsd: '$90.00 / hr',
        totalUsd: '$450.00 / wk'
      },
      {
        code: 'SURGE-120',
        description: 'Senior Calibration & Priority Playbook Engineering (Optional Surge Rate)',
        billingType: 'Hourly On-Demand',
        rateUsd: '$120.00 / hr',
        totalUsd: 'As Needed'
      }
    ],
    sovereignBoundaries: [
      '100% remote asynchronous execution envelope from Northwest Alabama operations center',
      'Guaranteed async capacity of >=10 hrs/week; baseline planned at 25 hrs/week (up to 40 hrs/week upon program demand)',
      'Provider supplies expert peer-level operational grading; downstream model training weights and platform outcomes remain client property & responsibility',
      'Out-of-scope code engineering outside the evaluation platform requires formal written change order'
    ],
    payoutFlowSummary:
      'Lili.co Commercial Checking (Sunrise Banks) under EIN 42-XXX-X843. Direct ACH inflow ($0 fee) -> Pre-tax business expenses deducted -> 25% tax reserve -> Bi-weekly owner sweep.'
  },
  {
    id: 'solutions-arch',
    code: 'WO-2026-1028-01',
    badge: 'ᚲ Architecture & SOW',
    title: 'Senior IT Systems Engineer & Solutions Architect Retainer',
    roleTitle: 'Senior IT Systems Engineer & Solutions Architect',
    targetClient: 'Enterprise Software & Compliance Vendors (Zantaz Data Resources Track)',
    workOrderId: 'DOC-001 • WO-2026-1028-01 / SOW-2026-1028-01',
    versionStatus: 'Rev. 1.0 • Active / Verified Production Scope',
    rateDescription: '$12,500.00 / month flat retainer ($150k annual contract value)',
    rateUnit: 'per month retainer',
    rateValueMonthly: 12500,
    pitchNote:
      'Senior IT Systems Engineer & Solutions Architect with verified production leadership in Microsoft 365 Enterprise, Exchange Online, Entra ID, Windows Server 2025, Linux infrastructure, and SOW standardization. Experienced in architecting isolated QA/presales lab environments, engineering AI-assisted deployment scripts (PowerShell/Bash), and leading technical discovery across complex archiving and compliance ecosystems (Zantaz EAS 10.0, Smart Stack 3.0, Logix Data Preserve). Available on monthly or milestone retainer with rapid 25% upfront mobilization draw.',
    workstationIsolation: [
      'Amazon Echo 5 drop-in collaboration endpoint hardware-quarantined on isolated 802.1Q IoT VLAN',
      'DENY all RFC1918 lateral LAN traffic; encrypted Quad9 9.9.9.9 DNS routing',
      'Hardware microphone mute engaged outside scheduled client operational hours',
      'Dedicated ReFS / Btrfs copy-on-write storage volumes for lab deployment testing'
    ],
    deliverables30_60_90: [
      {
        phase: 'Phase 1: Assess & Standardize',
        targetWindow: 'Days 1–30',
        deliverables: [
          'Master installation and migration procedures across enterprise archiving ecosystems',
          'Audit, update, and standardize professional services documentation, SOWs, and estimation templates',
          'Establish change-control governance workflows to eliminate scope creep and protect margins'
        ]
      },
      {
        phase: 'Phase 2: Build & Validate',
        targetWindow: 'Days 31–60',
        deliverables: [
          'Architect, deploy, and validate technical lab environments for presales demos and product QA',
          'Validate hybrid M365/Exchange Online and Windows Server 2025 cloud deployments',
          'Author and test deterministic cutover and rollback automation scripts (PowerShell & Bash)'
        ]
      },
      {
        phase: 'Phase 3: Deliver & Improve',
        targetWindow: 'Days 61–90',
        deliverables: [
          'Lead customer-facing enterprise deployments and tier-3 root-cause escalations',
          'Engineer reusable deployment, upgrade, and AI-assisted diagnostic scripts for support knowledgebases',
          'Conduct post-implementation reviews and milestone sign-offs under ESIGN standards'
        ]
      }
    ],
    lineItems: [
      {
        code: 'MOB-01',
        description: 'Onboarding & Lab Mobilization Draw (25% Month 1 Upfront Advance)',
        billingType: 'Flat Advance',
        rateUsd: '$3,125.00',
        totalUsd: '$3,125.00'
      },
      {
        code: 'RET-01',
        description: 'Professional Services & Deployment Coordination (Month 1 Net-30 Balance)',
        billingType: 'Net 30',
        rateUsd: '$9,375.00',
        totalUsd: '$9,375.00'
      },
      {
        code: 'RET-STD',
        description: 'Monthly Solutions Architecture & Deployment Retainer (Months 2–12)',
        billingType: 'Monthly Retainer',
        rateUsd: '$12,500.00 / mo',
        totalUsd: '$12,500.00 / mo'
      }
    ],
    sovereignBoundaries: [
      'Services delivered 100% remotely from Northwest Alabama operations center',
      'On-site customer travel capped at <= 15% and billed as pass-through travel expenses upon prior written authorization',
      'Third-party cloud hosting consumption fees and customer hardware procurement are excluded and billed directly to client',
      'Pre-existing customer hardware degradation and legacy archives prior to cutover remain client responsibility'
    ],
    payoutFlowSummary:
      'Lili.co Commercial Invoicing under EIN 42-XXX-X843. Mobilization advance ($3,125) initiates lab setup; Net-30 monthly ACH bridge with zero merchant interchange fees.'
  },
  {
    id: 'sow-governance',
    code: 'WO-2026-1028-03',
    badge: 'ᛏ SOW Governance',
    title: 'Statement of Work (SOW) Standardization & Scope Armor',
    roleTitle: 'Enterprise SOW Governance & Scoping Specialist',
    targetClient: 'IT Service Providers, Managed Service Firms & Tech Consultancies',
    workOrderId: 'DOC-001 • WO-2026-1028-03 / SOW-2026-1028-03',
    versionStatus: 'Rev. 1.0 • Active / Plug-and-Play Fixed Packs',
    rateDescription: '$1,500.00 – $3,500.00 flat fee per standardized playbook',
    rateUnit: 'per package / deliverable',
    rateValueMonthly: 3500,
    pitchNote:
      'Standardize your technical delivery with ironclad, battle-tested Statement of Work (SOW) architecture. Incorporates statutory ESIGN / Alabama UETA electronic execution blocks, NAICS code compliance, clear 30/60/90-day verification gates, hardware isolation boundaries, and deterministic change-order protocols that protect delivery margins and eliminate unbillable scope creep.',
    workstationIsolation: [
      'Zero-custody template drafting and automated redline purging',
      'Standardized IRC § 6501 7-year audit retention compliance',
      'Client data partitioned in isolated customer directories'
    ],
    deliverables30_60_90: [
      {
        phase: 'Phase 1: Contract & Scope Audit',
        targetWindow: 'Day 1–7',
        deliverables: [
          'Audit existing contract terms, SOWs, and proposal templates for liability exposure',
          'Identify uncompensated milestone traps and missing change-order trigger mechanisms'
        ]
      },
      {
        phase: 'Phase 2: Master Template Standardization',
        targetWindow: 'Day 8–14',
        deliverables: [
          'Draft custom Master SOW and 90-Day Onboarding Dossier tailored to firm services',
          'Embed statutory ESIGN/UETA execution blocks, EIN/NAICS mappings, and payment schedules',
          'Structure 3-phase verification gates with unambiguous acceptance criteria'
        ]
      },
      {
        phase: 'Phase 3: Change Control Playbook Delivery',
        targetWindow: 'Day 15–21',
        deliverables: [
          'Deliver automated Change Order request forms and rate band surcharge schedules',
          'Provide training cheat-sheet for presales discovery calls and pricing defensibility'
        ]
      }
    ],
    lineItems: [
      {
        code: 'SOW-AUDIT',
        description: 'Baseline SOW & Proposal Risk Audit',
        billingType: 'Fixed Pack',
        rateUsd: '$1,500.00',
        totalUsd: '$1,500.00'
      },
      {
        code: 'SOW-MASTER',
        description: 'Complete Master SOW & 90-Day Execution Dossier System',
        billingType: 'Fixed Pack',
        rateUsd: '$3,500.00',
        totalUsd: '$3,500.00'
      }
    ],
    sovereignBoundaries: [
      'Templates provide operational and structural contracting best practices',
      'Final legal representation and formal court filings are referred to client legal counsel',
      'Delivered asynchronously in clean Markdown, Plaintext, and PDF-ready formats'
    ],
    payoutFlowSummary:
      '50% deposit upon kickoff, 50% upon final playbook delivery via Lili.co ACH / Commercial invoice.'
  }
];

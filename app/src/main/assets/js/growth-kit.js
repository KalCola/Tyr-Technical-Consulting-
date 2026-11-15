// File: js/growth-kit.js
/**
 * TYR Protocol — Zero-Cost Self-Marketing & Organic Growth Engine
 * Pure-Client QR Matrix Generator, SKU/Referral Deep-Link Router,
 * 60s Quick Audit Quiz, One-Tap Local Pitch Templates, Printable QR Flyers,
 * Anonymized NW Alabama Proof Case Studies, and Service-Area GBP Checklist.
 */
(function () {
  'use strict';

  const STORAGE_OWN_REF_KEY = 'tyr_own_referral_code_v1';
  const STORAGE_ACTIVE_REF_KEY = 'tyr_active_inbound_ref_v1';
  const STORAGE_GROWTH_LEDGER_KEY = 'tyr_growth_attribution_v1';
  const STORAGE_GBP_CHECKLIST_KEY = 'tyr_gbp_checklist_v1';

  const VCARD_PAYLOAD = [
    'BEGIN:VCARD',
    'VERSION:3.0',
    'N:Holcomb;Caleb;TR;;',
    'FN:Caleb Holcomb — TYR Technical Consulting',
    'ORG:TYR Technical Consulting (CALEB TR HOLCOMB d/b/a TYR)',
    'TITLE:Principal Systems Engineer & Expense Auditor',
    'TEL;TYPE=WORK,VOICE,CELL,MSG:+16592578043',
    'EMAIL;TYPE=WORK,PREF,INTERNET:tyr@tyrprotocol.pro',
    'EMAIL;TYPE=HOME,INTERNET:colacal43@gmail.com',
    'URL;TYPE=WORK:https://tyrprotocol.pro',
    'URL;TYPE=GitHub:https://github.com/kalcola/tyrs-technical-consultation-',
    'ADR;TYPE=WORK:;;;Northwest Alabama;AL;;United States',
    'NOTE:NAICS 541512 & 541611. 100% Remote & Northwest Alabama. Zero-Custody 0-Day Data Purge.',
    'END:VCARD'
  ].join('\r\n');

  const MECARD_QR_STRING =
    'MECARD:N:Holcomb,Caleb;TEL:+16592578043;EMAIL:tyr@tyrprotocol.pro;URL:https://tyrprotocol.pro;NOTE:TYR Technical Consulting;;';

  // --- Minimal Pure-Client ISO/IEC 18004 QR Code Encoder (Versions 1–6, ECC-L, Byte Mode) ---
  const QR_CAPACITY_L = [
    null,
    { ver: 1, size: 21, dataCw: 19, eccPerBlock: 7, blocks: 1, align: [] },
    { ver: 2, size: 25, dataCw: 34, eccPerBlock: 10, blocks: 1, align: [6, 18] },
    { ver: 3, size: 29, dataCw: 55, eccPerBlock: 15, blocks: 1, align: [6, 22] },
    { ver: 4, size: 33, dataCw: 80, eccPerBlock: 20, blocks: 1, align: [6, 26] },
    { ver: 5, size: 37, dataCw: 108, eccPerBlock: 26, blocks: 1, align: [6, 30] },
    { ver: 6, size: 41, dataCw: 136, eccPerBlock: 18, blocks: 2, align: [6, 34] }
  ];

  const GF_EXP = new Uint8Array(512);
  const GF_LOG = new Uint8Array(256);
  (function initGalois() {
    let x = 1;
    for (let i = 0; i < 255; i++) {
      GF_EXP[i] = x;
      GF_LOG[x] = i;
      x <<= 1;
      if (x & 0x100) x ^= 0x11d;
    }
    for (let i = 255; i < 512; i++) GF_EXP[i] = GF_EXP[i - 255];
  })();

  function gfMul(a, b) {
    if (a === 0 || b === 0) return 0;
    return GF_EXP[GF_LOG[a] + GF_LOG[b]];
  }

  function rsGeneratorPoly(deg) {
    let poly = [1];
    for (let i = 0; i < deg; i++) {
      const next = new Array(poly.length + 1).fill(0);
      const factor = GF_EXP[i];
      for (let j = 0; j < poly.length; j++) {
        next[j] ^= poly[j];
        next[j + 1] ^= gfMul(poly[j], factor);
      }
      poly = next;
    }
    return poly;
  }

  function rsComputeEcc(data, eccLen) {
    const gen = rsGeneratorPoly(eccLen);
    const res = new Array(data.length + eccLen).fill(0);
    for (let i = 0; i < data.length; i++) res[i] = data[i];
    for (let i = 0; i < data.length; i++) {
      const coef = res[i];
      if (coef !== 0) {
        for (let j = 1; j < gen.length; j++) {
          res[i + j] ^= gfMul(gen[j], coef);
        }
      }
    }
    return res.slice(data.length);
  }

  function buildQrMatrix(text) {
    const bytes = new TextEncoder().encode(String(text || 'https://tyrprotocol.pro'));
    let spec = null;
    for (let v = 1; v <= 6; v++) {
      // 4 bits mode + 8 bits len + bytes*8 + 4 bits term => bytes + 2 bytes overhead
      if (bytes.length + 2 <= QR_CAPACITY_L[v].dataCw) {
        spec = QR_CAPACITY_L[v];
        break;
      }
    }
    if (!spec) {
      spec = QR_CAPACITY_L[6];
    }
    const maxPayload = Math.min(bytes.length, spec.dataCw - 2);
    const bits = [];
    const pushBits = (val, len) => {
      for (let i = len - 1; i >= 0; i--) bits.push((val >> i) & 1);
    };
    pushBits(0b0100, 4); // Byte mode
    pushBits(maxPayload, 8);
    for (let i = 0; i < maxPayload; i++) pushBits(bytes[i], 8);
    const totalDataBits = spec.dataCw * 8;
    const termLen = Math.min(4, totalDataBits - bits.length);
    pushBits(0, termLen);
    while (bits.length % 8 !== 0) bits.push(0);

    const dataCw = [];
    for (let i = 0; i < bits.length; i += 8) {
      let b = 0;
      for (let j = 0; j < 8; j++) b = (b << 1) | bits[i + j];
      dataCw.push(b);
    }
    let padToggle = 0;
    while (dataCw.length < spec.dataCw) {
      dataCw.push(padToggle % 2 === 0 ? 0xec : 0x11);
      padToggle++;
    }

    const blockSize = Math.floor(spec.dataCw / spec.blocks);
    const dataBlocks = [];
    const eccBlocks = [];
    for (let b = 0; b < spec.blocks; b++) {
      const slice = dataCw.slice(b * blockSize, (b + 1) * blockSize);
      dataBlocks.push(slice);
      eccBlocks.push(rsComputeEcc(slice, spec.eccPerBlock));
    }

    const codewords = [];
    for (let i = 0; i < blockSize; i++) {
      for (let b = 0; b < spec.blocks; b++) codewords.push(dataBlocks[b][i]);
    }
    for (let i = 0; i < spec.eccPerBlock; i++) {
      for (let b = 0; b < spec.blocks; b++) codewords.push(eccBlocks[b][i]);
    }

    const N = spec.size;
    const mod = Array.from({ length: N }, () => new Int8Array(N).fill(-1));
    const isFunc = Array.from({ length: N }, () => new Uint8Array(N));

    const setFunc = (r, c, val) => {
      if (r >= 0 && r < N && c >= 0 && c < N) {
        mod[r][c] = val ? 1 : 0;
        isFunc[r][c] = 1;
      }
    };

    const drawFinder = (r0, c0) => {
      for (let dr = -1; dr <= 7; dr++) {
        for (let dc = -1; dc <= 7; dc++) {
          const r = r0 + dr;
          const c = c0 + dc;
          if (r < 0 || r >= N || c < 0 || c >= N) continue;
          const on =
            dr >= 0 &&
            dr <= 6 &&
            dc >= 0 &&
            dc <= 6 &&
            (dr === 0 || dr === 6 || dc === 0 || dc === 6 || (dr >= 2 && dr <= 4 && dc >= 2 && dc <= 4));
          setFunc(r, c, on ? 1 : 0);
        }
      }
    };

    drawFinder(0, 0);
    drawFinder(0, N - 7);
    drawFinder(N - 7, 0);

    for (let i = 8; i < N - 8; i++) {
      setFunc(6, i, i % 2 === 0 ? 1 : 0);
      setFunc(i, 6, i % 2 === 0 ? 1 : 0);
    }

    if (spec.align.length > 0) {
      for (const ar of spec.align) {
        for (const ac of spec.align) {
          if (isFunc[ar][ac]) continue;
          for (let dr = -2; dr <= 2; dr++) {
            for (let dc = -2; dc <= 2; dc++) {
              const on = Math.abs(dr) === 2 || Math.abs(dc) === 2 || (dr === 0 && dc === 0);
              setFunc(ar + dr, ac + dc, on ? 1 : 0);
            }
          }
        }
      }
    }

    setFunc(N - 8, 8, 1);
    for (let i = 0; i < 9; i++) {
      if (i !== 6) {
        isFunc[8][i] = 1;
        isFunc[i][8] = 1;
      }
    }
    for (let i = 0; i < 8; i++) {
      isFunc[8][N - 1 - i] = 1;
      if (i < 7) isFunc[N - 1 - i][8] = 1;
    }

    // Place data bits (zigzag)
    const allBits = [];
    for (const cw of codewords) {
      for (let b = 7; b >= 0; b--) allBits.push((cw >> b) & 1);
    }
    let bitIdx = 0;
    let up = true;
    for (let col = N - 1; col >= 1; col -= 2) {
      if (col === 6) col--;
      for (let step = 0; step < N; step++) {
        const row = up ? N - 1 - step : step;
        for (let dx = 0; dx < 2; dx++) {
          const c = col - dx;
          if (!isFunc[row][c]) {
            const bit = bitIdx < allBits.length ? allBits[bitIdx++] : 0;
            mod[row][c] = bit;
          }
        }
      }
      up = !up;
    }

    // Apply Mask 0 ((r + c) % 2 === 0) and write Format Info for ECC-L (01) + Mask 0 (000) -> 0x77C4
    for (let r = 0; r < N; r++) {
      for (let c = 0; c < N; c++) {
        if (!isFunc[r][c] && (r + c) % 2 === 0) {
          mod[r][c] ^= 1;
        }
      }
    }

    // Format bits for ECC-L (01) + Mask 0 (000): 111011111000100 (MSB to LSB)
    const fmt = 0b111011111000100;
    for (let i = 0; i < 15; i++) {
      const bit = (fmt >> (14 - i)) & 1;
      // Copy 1: around top-left finder
      if (i < 6) mod[8][i] = bit;
      else if (i === 6) mod[8][7] = bit;
      else if (i === 7) mod[8][8] = bit;
      else if (i === 8) mod[7][8] = bit;
      else mod[14 - i][8] = bit;

      // Copy 2: bottom-left vertical + top-right horizontal
      if (i < 7) mod[N - 1 - i][8] = bit;
      else mod[8][N - 15 + i] = bit;
    }

    return mod;
  }

  function renderQrSvg(text, darkHex = '#090a0f', lightHex = '#ffffff', sizePx = 180) {
    const matrix = buildQrMatrix(text);
    const n = matrix.length;
    const quiet = 2;
    const total = n + quiet * 2;
    let paths = '';
    for (let r = 0; r < n; r++) {
      for (let c = 0; c < n; c++) {
        if (matrix[r][c] === 1) {
          paths += `M${c + quiet},${r + quiet}h1v1h-1z`;
        }
      }
    }
    return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${total} ${total}" width="${sizePx}" height="${sizePx}" role="img" aria-label="QR Code for ${String(text).replace(/"/g, '')}"><rect width="${total}" height="${total}" fill="${lightHex}" rx="2"/><path d="${paths}" fill="${darkHex}"/></svg>`;
  }

  // --- Data Stores ---
  function getOwnReferralCode() {
    return (localStorage.getItem(STORAGE_OWN_REF_KEY) || 'TYR-843-REF').trim().toUpperCase();
  }

  function setOwnReferralCode(code) {
    const clean = String(code || 'TYR-843-REF')
      .trim()
      .toUpperCase()
      .replace(/[^A-Z0-9_-]/g, '-');
    localStorage.setItem(STORAGE_OWN_REF_KEY, clean || 'TYR-843-REF');
    return getOwnReferralCode();
  }

  function getActiveReferralCode() {
    return (localStorage.getItem(STORAGE_ACTIVE_REF_KEY) || '').trim().toUpperCase();
  }

  function setActiveReferralCode(code) {
    const clean = String(code || '')
      .trim()
      .toUpperCase()
      .replace(/[^A-Z0-9_-]/g, '-');
    if (clean) localStorage.setItem(STORAGE_ACTIVE_REF_KEY, clean);
    else localStorage.removeItem(STORAGE_ACTIVE_REF_KEY);
    return getActiveReferralCode();
  }

  function getGrowthLedger() {
    try {
      const raw = localStorage.getItem(STORAGE_GROWTH_LEDGER_KEY);
      if (raw) return JSON.parse(raw);
    } catch {}
    return {
      sharesDispatched: 0,
      quizCompletions: 0,
      referredVisits: 0,
      creditsIssuedUsd: 0,
      events: []
    };
  }

  function recordGrowthEvent(type, detail, creditUsd = 0) {
    const ledger = getGrowthLedger();
    if (type === 'SHARE') ledger.sharesDispatched += 1;
    if (type === 'QUIZ') ledger.quizCompletions += 1;
    if (type === 'REF_VISIT') ledger.referredVisits += 1;
    if (creditUsd > 0) ledger.creditsIssuedUsd += creditUsd;
    ledger.events.unshift({
      type,
      detail,
      creditUsd,
      timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19) + ' UTC'
    });
    ledger.events = ledger.events.slice(0, 20);
    localStorage.setItem(STORAGE_GROWTH_LEDGER_KEY, JSON.stringify(ledger));
    return ledger;
  }

  function downloadVCard() {
    const blob = new Blob([VCARD_PAYLOAD], { type: 'text/vcard;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'TYR_Caleb_Holcomb.vcf';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    recordGrowthEvent('SHARE', 'Downloaded TYR_Caleb_Holcomb.vcf contact card');
  }

  function buildShareUrl(sku = '', includeRef = true) {
    const url = new URL('https://tyrprotocol.pro/');
    if (sku) url.searchParams.set('sku', sku);
    if (includeRef) url.searchParams.set('ref', getOwnReferralCode());
    return url.toString();
  }

  function buildPitchText(channel, service, includeRef = true) {
    const sku = service ? service.sku : '';
    const title = service ? service.title : 'Zero-Lock-In Websites, Domain Shield & Expense Audits';
    const price = service ? service.price : '$135–$295 One-Time';
    const link = buildShareUrl(sku, includeRef);
    const refSnippet = includeRef ? `Use referral code ${getOwnReferralCode()} for a 10% Welcome Credit.` : '';

    if (channel === 'NEXTDOOR') {
      return `Hey Northwest Alabama neighbors — if you're a local tradesperson, small business, or independent contractor tired of $30–$100/mo website builders or bloated vendor bills, I run TYR Technical Consulting (NAICS 541512 & 541611).\n\nFeatured offer: ${title} (${price}) — zero recurring platform hostage fees and 100% master code ownership.\n${refSnippet}\n\nInspect full transparent pricing & run a free 60s audit here: ${link} or call/text (659) 257-8043.`;
    }
    if (channel === 'FACEBOOK') {
      return `[Northwest Alabama & Remote] Hardened Technical & Admin Deliverables — Zero Monthly Hostage Fees\n\n⚡ ${title} — ${price}\n✓ 100% client ownership of code & master files\n✓ Zero-Custody guarantee (0-day raw statement purge)\n✓ Optional 50% deposit / milestone split via Stripe\n${refSnippet ? '✓ ' + refSnippet + '\n' : ''}\nTap to view specs, instant quote cart, or 60s self-audit: ${link}\n📞 (659) 257-8043`;
    }
    if (channel === 'EMAIL') {
      return `Subject: TYR Technical Consulting — ${title} (${price})\n\nHello,\n\nSharing a direct brief for ${title} (${price}, NAICS 541512 / 541611). Every deliverable ships with 100% client master ownership, SHA-384 integrity receipts, and zero mandatory recurring platform fees.\n\n${refSnippet ? refSnippet + '\n\n' : ''}Review scope & schedule directly: ${link}\nDirect Line: (659) 257-8043 | tyr@tyrprotocol.pro`;
    }
    // SMS default
    return `TYR Technical Consulting (NW Alabama & Remote): ${title} is ${price} with $0/mo hosting lock-in. ${refSnippet} View specs & self-audit: ${link} or call (659) 257-8043.`;
  }

  // --- 60-Second Quick Audit Quiz ---
  const QUIZ_QUESTIONS = [
    {
      id: 'q_web',
      prompt: '1. How much do you currently pay per month for your website & hosting?',
      options: [
        { label: '$30–$150/mo (Wix, Squarespace, GoDaddy, agency)', scorePenalty: 25, skus: ['SVC-WEB-01'] },
        { label: 'No website yet — relying only on Facebook/word of mouth', scorePenalty: 20, skus: ['SVC-WEB-01', 'SVC-KIT-03'] },
        { label: '$0/mo static site I fully control', scorePenalty: 0, skus: [] }
      ]
    },
    {
      id: 'q_email',
      prompt: '2. Is your business email protected with custom-domain DMARC, SPF & DKIM?',
      options: [
        { label: 'Using @gmail.com / @yahoo.com or unsure about DMARC', scorePenalty: 25, skus: ['SVC-DOM-02', 'SVC-B2B-17'] },
        { label: 'Domain exists but spoofing/spam filters are weak', scorePenalty: 15, skus: ['SVC-DOM-02'] },
        { label: 'Hardened p=reject DMARC & DKIM enforced', scorePenalty: 0, skus: [] }
      ]
    },
    {
      id: 'q_legal',
      prompt: '3. Do you have written SOWs/Contracts & E-Sign ready before taking client deposits?',
      options: [
        { label: 'Verbal handshakes or recycled Word docs', scorePenalty: 25, skus: ['SVC-OPS-05', 'SVC-B2B-16'] },
        { label: 'Basic template, no cryptographic audit receipt', scorePenalty: 15, skus: ['SVC-OPS-05'] },
        { label: 'Signed MSA/SOW + Stripe milestone splits active', scorePenalty: 0, skus: [] }
      ]
    },
    {
      id: 'q_bills',
      prompt: '4. When did you last audit ISP, cell, SaaS subscriptions, and Schedule C tracking?',
      options: [
        { label: 'Over a year ago — paying mystery creeping vendor fees', scorePenalty: 25, skus: ['SVC-AUD-09', 'SVC-CMD-07'] },
        { label: 'Need organized tax-ready Schedule C & legacy vault', scorePenalty: 15, skus: ['SVC-TAX-13', 'SVC-VLT-08'] },
        { label: 'Audited quarterly with 0-day statement hygiene', scorePenalty: 0, skus: [] }
      ]
    }
  ];

  function evaluateQuiz(answersMap) {
    let penalty = 0;
    const matchedSkuCounts = {};
    const riskFlags = [];
    QUIZ_QUESTIONS.forEach((q) => {
      const idx = Number(answersMap[q.id] ?? 0);
      const opt = q.options[idx] || q.options[0];
      penalty += opt.scorePenalty;
      if (opt.scorePenalty > 0) riskFlags.push(opt.label);
      opt.skus.forEach((sku) => {
        matchedSkuCounts[sku] = (matchedSkuCounts[sku] || 0) + 1;
      });
    });
    const score = Math.max(0, 100 - penalty);
    const recommendedSkus = Object.keys(matchedSkuCounts).slice(0, 3);
    if (recommendedSkus.length === 0) recommendedSkus.push('SVC-SEC-12', 'SVC-CMD-07');
    return {
      score,
      riskFlags,
      recommendedSkus,
      estAnnualLeakUsd: penalty * 18
    };
  }

  // --- Anonymized NW Alabama Proof Case Studies (OPSEC Clean) ---
  const CASE_STUDIES = [
    {
      id: 'CS-NWA-01',
      region: 'Northwest Alabama — Independent Field Trades',
      headline: 'Eliminated $588/yr Hosting Rent + Doubled Estimate Callbacks',
      sku: 'SVC-WEB-01 + SVC-KIT-03',
      beforeState: 'Paying $49/mo for a slow drag-and-drop builder with no offline contact card.',
      deliverable: 'Static $0/mo mobile-first storefront, truck-magnet QR flyer, and instant .vcf intake.',
      verifiedOutcome: '$588/yr fixed cost eliminated • 48h turnaround • 100% master Git repo ownership.'
    },
    {
      id: 'CS-NWA-02',
      region: 'Muscle Shoals Corridor — B2B Service Operator',
      headline: 'Stopped Vendor Email Spoofing & Secured Wire Instructions',
      sku: 'SVC-DOM-02 + SVC-B2B-17',
      beforeState: 'Sending invoices from free webmail; invoices landing in client spam folders.',
      deliverable: 'SPF/DKIM/DMARC p=reject DNS lockdown + wire verification SOP + 2FA key runbook.',
      verifiedOutcome: '100% DMARC compliance • Zero SaaS retainers added • Audit delivered in 36h.'
    },
    {
      id: 'CS-NWA-03',
      region: 'Northwest Alabama — Sole Proprietor (1099)',
      headline: 'Clawed Back $74/mo in ISP/SaaS Creep + Clean Schedule C Ledger',
      sku: 'SVC-AUD-09 + SVC-TAX-13',
      beforeState: 'Unchecked telecom surcharges & uncategorized receipts causing tax-season friction.',
      deliverable: 'ISP fee dispute escalation script + offline CSV/MD Schedule C ledger (0-day statement purge).',
      verifiedOutcome: '$888/yr recurring overhead cut • Zero raw bank files retained on disk.'
    },
    {
      id: 'CS-NWA-04',
      region: 'Remote US Subcontractor — Technical Consulting',
      headline: 'Standardized E-Sign SOW & 50% Deposit Split Flow',
      sku: 'SVC-OPS-05 + SVC-B2B-16',
      beforeState: 'Scope creep and delayed invoice settlements without binding signed change orders.',
      deliverable: '3-Document E-Sign SOW/MSA Kit with SHA-384 receipt chain & Stripe 50/50 split.',
      verifiedOutcome: '0 unpaid kickoff hours • Cryptographically hashed agreements from day one.'
    }
  ];

  // --- Zero-Cost Google Business Profile & Local Directory Checklist ---
  const GBP_CHECKLIST_ITEMS = [
    {
      id: 'gbp_sab',
      title: 'Google Business Profile — Service-Area Mode (Hide Home Street)',
      cost: '$0',
      detail: 'Select "Service-Area Business" (No physical storefront), toggle OFF street address, and set coverage to Northwest Alabama & United States.'
    },
    {
      id: 'gbp_cats',
      title: 'Set Primary Category: Computer Support & Services / IT Consultant',
      cost: '$0',
      detail: 'Add secondary categories "Web Designer" & "Business Management Consultant" (NAICS 541512 & 541611) + paste all 31 SKUs into Products.'
    },
    {
      id: 'apple_connect',
      title: 'Apple Business Connect (Siri, Apple Maps & Spotlight)',
      cost: '$0',
      detail: 'Register CALEB TR HOLCOMB d/b/a TYR with Service-Area posture, tyrprotocol.pro URL, and +1-659-257-8043.'
    },
    {
      id: 'bing_places',
      title: 'Bing Places for Business (Syncs Edge, Windows & Copilot Search)',
      cost: '$0',
      detail: 'Import directly from your verified Google Business Profile in 2 minutes at zero cost.'
    },
    {
      id: 'nextdoor_biz',
      title: 'Nextdoor Free Business Page + Neighbor Recommendation Link',
      cost: '$0',
      detail: 'Claim your Northwest Alabama Business Page and post 1 helpful scam-alert/cost-cut tip per week with your ?sku= link.'
    },
    {
      id: 'nap_audit',
      title: 'NAP + GitHub Link Consistency Check',
      cost: '$0',
      detail: 'Ensure exact match everywhere: "TYR Technical Consulting", "(659) 257-8043", "tyr@tyrprotocol.pro", "https://tyrprotocol.pro".'
    },
    {
      id: 'review_qr',
      title: 'Print Leave-Behind QR Pocket Cards on Completion',
      cost: '$0–Home Ink',
      detail: 'Hand every closed client a 3.5x2 leave-behind card carrying your referral code (10% credit) + instant Google Review QR link.'
    }
  ];

  function getChecklistState() {
    try {
      const raw = localStorage.getItem(STORAGE_GBP_CHECKLIST_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch {
      return {};
    }
  }

  function toggleChecklistItem(id) {
    const state = getChecklistState();
    state[id] = !state[id];
    localStorage.setItem(STORAGE_GBP_CHECKLIST_KEY, JSON.stringify(state));
    return state;
  }

  function buildPrintableCollateralHtml({ mode, targetUrl, skuTitle, skuPrice, includeRef }) {
    const qrSvg = renderQrSvg(targetUrl, '#090a0f', '#ffffff', mode === 'CARD' ? 120 : 210);
    const refLine = includeRef ? `REFERRAL CODE: ${getOwnReferralCode()} (10% CREDIT)` : 'DIRECT BOOKING';
    const isCard = mode === 'CARD';

    return `<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><title>TYR Printable ${isCard ? 'Pocket Card' : 'Flyer'}</title>
<style>
  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #fff; color: #090a0f; margin: 0; padding: 20px; }
  .sheet { max-width: ${isCard ? '420px' : '740px'}; margin: 0 auto; border: 2px solid #090a0f; border-radius: 10px; padding: ${isCard ? '16px' : '28px'}; }
  .top { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #dc2626; padding-bottom: 10px; margin-bottom: 14px; }
  .brand { font-size: ${isCard ? '15px' : '24px'}; font-weight: 900; letter-spacing: 0.04em; }
  .sub { font-size: ${isCard ? '10px' : '13px'}; color: #475569; font-weight: 600; }
  .grid { display: grid; grid-template-columns: 1fr auto; gap: 16px; align-items: center; }
  .offer { font-size: ${isCard ? '14px' : '20px'}; font-weight: 800; color: #991b1b; margin: 6px 0; }
  ul { margin: 8px 0; padding-left: 18px; font-size: ${isCard ? '11px' : '14px'}; line-height: 1.5; }
  .badge { display: inline-block; background: #090a0f; color: #fbbf24; font-family: monospace; font-size: ${isCard ? '10px' : '12px'}; font-weight: 700; padding: 5px 10px; border-radius: 4px; margin-top: 8px; }
  .qr-box { text-align: center; border: 1px solid #cbd5e1; padding: 8px; border-radius: 8px; }
  .qr-cap { font-family: monospace; font-size: 9px; font-weight: 700; margin-top: 4px; color: #334155; }
  .foot { border-top: 1px dashed #94a3b8; margin-top: 14px; padding-top: 8px; font-size: ${isCard ? '10px' : '12px'}; display: flex; justify-content: space-between; flex-wrap: wrap; gap: 6px; font-weight: 700; }
  @media print { body { padding: 0; } .no-print { display: none; } }
</style></head><body>
<div class="sheet">
  <div class="top">
    <div>
      <div class="brand">ᛏ TYR TECHNICAL CONSULTING</div>
      <div class="sub">CALEB TR HOLCOMB d/b/a TYR • Northwest Alabama &amp; Nationwide Remote</div>
    </div>
    <div style="font-family:monospace;font-size:11px;font-weight:800;">NAICS 541512 / 541611</div>
  </div>
  <div class="grid">
    <div>
      <div class="offer">${skuTitle || 'Zero-Lock-In Websites, Domain Shield & Expense Audits'} ${skuPrice ? '— ' + skuPrice : ''}</div>
      <ul>
        <li><strong>$0/mo Platform Lock-In:</strong> 100% client master code &amp; file ownership.</li>
        <li><strong>Zero-Custody Guarantee:</strong> Raw statements purged (0-Day). SHA-384 receipts.</li>
        <li><strong>Flexible Settlement:</strong> 100%, 50% Deposit, or Milestone splits via Stripe.</li>
      </ul>
      <div class="badge">${refLine}</div>
    </div>
    <div class="qr-box">
      ${qrSvg}
      <div class="qr-cap">SCAN TO LAUNCH</div>
    </div>
  </div>
  <div class="foot">
    <span>📞 (659) 257-8043</span>
    <span>✉️ tyr@tyrprotocol.pro</span>
    <span>🌐 tyrprotocol.pro</span>
  </div>
</div>
<div class="no-print" style="text-align:center;margin-top:16px;">
  <button onclick="window.print()" style="padding:10px 20px;background:#dc2626;color:#fff;border:none;border-radius:6px;font-weight:700;cursor:pointer;">🖨️ Print / Save PDF</button>
</div>
</body></html>`;
  }

  window.TyrGrowth = {
    VCARD_PAYLOAD,
    MECARD_QR_STRING,
    QUIZ_QUESTIONS,
    CASE_STUDIES,
    GBP_CHECKLIST_ITEMS,
    buildQrMatrix,
    renderQrSvg,
    getOwnReferralCode,
    setOwnReferralCode,
    getActiveReferralCode,
    setActiveReferralCode,
    getGrowthLedger,
    recordGrowthEvent,
    downloadVCard,
    buildShareUrl,
    buildPitchText,
    evaluateQuiz,
    getChecklistState,
    toggleChecklistItem,
    buildPrintableCollateralHtml
  };
})();

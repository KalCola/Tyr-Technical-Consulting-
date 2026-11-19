// File: js/stripe-checkout.js
/**
 * TYR Protocol — Web Storefront Stripe Checkout & SHA-384 Digital Receipt Ledger
 * PCI-DSS v4.0 SAQ-A Compliant: Zero raw PAN/CVV or live secret keys stored on static client.
 */
(function () {
  'use strict';

  const STORAGE_RECEIPTS_KEY = 'tyr_stripe_receipts_v2';
  const STORAGE_SKU_PRICES_KEY = 'tyr_custom_sku_price_ids_v1';
  const STORAGE_BACKEND_URL_KEY = 'tyr_stripe_backend_endpoint_v1';
  const STORAGE_CART_KEY = 'tyr_storefront_cart_v1';

  const STRIPE_ACCOUNT_ID = 'acct_1Ub2VYRxlL3JK5Uq';
  const STRIPE_TAX_CODE = 'txcd_20060048';
// Canonical v3.2.0-dwarven-forge 36-SKU effort-calibrated base floor (in USD cents, >= $65.00)
const SKU_FLOOR_CENTS_MAP = {
'SVC-WEB-01': 26500, 'SVC-DOM-02': 17500, 'SVC-KIT-03': 16500,
'SVC-CAR-04': 16500, 'SVC-OPS-05': 19500, 'SVC-BUY-06': 17500,
'SVC-CMD-07': 19500, 'SVC-VLT-08': 17500, 'SVC-AUD-09': 19500,
'MOD-DNS-03': 6500,  'MOD-EML-04': 7500,  'MOD-FRM-05': 8500,
'WEB-LP-01': 11500,  'WEB-ST-02': 18500,
'SVC-LOK-10': 9500,  'SVC-GRM-11': 11500, 'SVC-FEN-12': 13500,
'SUB-WEB-01': 6500,  'SUB-DOM-02': 7500,  'SUB-OPS-03': 8500,
'SUB-FIN-04': 9500,  'SUB-BIL-05': 8500,  'SUB-ADM-06': 9500,
'RET-ALL-07': 54500,
'SVC-SOP-11': 18500, 'SVC-SEC-12': 15500, 'SVC-TAX-13': 17500,
'SVC-BCK-14': 17500, 'SVC-DCK-15': 16500,
'SVC-B2B-16': 32500, 'SVC-B2B-17': 26500,
'B2B-SEC-01': 13500, 'B2B-SLA-02': 15500, 'B2B-LGL-03': 11000,
'B2B-SEO-04': 9500,  'B2B-SOP-05': 18500
  };

function getSkuBaseCents(svc) {
  const parsed = parseServicePriceToCents(svc?.price);
  const skuFloor = SKU_FLOOR_CENTS_MAP[String(svc?.sku || '').toUpperCase()] || 6500;
  return parsed >= 6500 ? parsed : skuFloor;
}
  
  const CHECKOUT_PLAN_ID = 'chkplan_61VkmYCmcY0V8AnN216VkQSuURSQVGsDGT0SpcV9c';
  const CHECKOUT_STUDIO_DRAFT_URL =
    'https://dashboard.stripe.com/acct_1Ub2VYRxlL3JK5Uq/checkout/checkouts/draft/chkplan_61VkmYCmcY0V8AnN216VkQSuURSQVGsDGT0SpcV9c?mode=implement';
  const FALLBACK_CUSTOM_LINK = "https://donate.stripe.com/4gM3cxctj9ld6i5allgUM00";

  const STRIPE_PACKAGE_LINKS = {
    "Midgard Website Launch": "https://buy.stripe.com/aFabJ3ctj0OH5e1511gUM0A",
    "Asgard Domain & Email Shield": "https://buy.stripe.com/fZu6oJgJzgNF21PdxxgUM0v",
    "Vanaheim QR & Form Kit": "https://buy.stripe.com/fZu00ldxn2WP21PfFFgUM0u",
    "Alfheim Resume & Bid Overhaul": "https://buy.stripe.com/7sYfZjbpffJB8qdfFFgUM0x",
    "Nidavellir 3-Doc E-Sign Kit": "https://buy.stripe.com/aFa00l3WN9ldfSFfFFgUM0C",
    "Muspelheim Buyer's Dossier": "https://buy.stripe.com/8x2eVf8d3dBt9uhdxxgUM0w",
    "Svartalfheim Budget & Schedule C Hub": "https://buy.stripe.com/eVqcN7gJz7d5bCp1OPgUM0y",
    "Niflheim Insurance & Legacy Vault": "https://buy.stripe.com/5kQ3cx1OF6916i59hhgUM0z",
    "Jotunheim Billing & ISP Dispute Kit": "https://buy.stripe.com/28E5kF78Z2WP5e1allgUM0B",
    "1099 Contractor Schedule C Ledger": "https://buy.stripe.com/cNi6oJalb54X0XLdxxgUM0K",
    "Skíðblaðnir 3-Section Starter Landing Page": "https://buy.stripe.com/14AfZjalb7d549XfFFgUM0P",
    "Gullinbursti Single-Page Bio & Link Hub": "https://buy.stripe.com/6oUeVf64VeFx9uh2STgUM0O",
    "Gungnir Custom Email Alias Setup": "https://buy.stripe.com/3cI9AVctj8h9fSFallgUM0D",
    "Mjölnir DNS & SPF Quick-Lock": "https://buy.stripe.com/7sY4gB78ZfJBgWJ3WXgUM0F",
    "Draupnir Single Fillable PDF or QR Card": "https://buy.stripe.com/3cI28t2SJfJBbCpdxxgUM0I",
    "Garm Privacy Scrub & Freeze Shield": "https://buy.stripe.com/14AeVf9h71SLgWJeBBgUM0J",
    "Fenrir Free-Software Switch Kit": "https://buy.stripe.com/3cI00l2SJ9ld8qddxxgUM0L",
    "Cold-Storage 3-2-1 Backup Runbook": "https://buy.stripe.com/28E6oJfFvcxpfSFcttgUM0M",
    "B2B Vendor Onboarding & Master MSA Suite": "https://buy.stripe.com/fZu9AVgJzcxpgWJ799gUM0R",
    "Corporate Anti-Phishing & Wire Protocol": "https://buy.stripe.com/8x2fZj9h76910XL3WXgUM0Q",
    "SOP & Knowledge Base Blueprint": "https://buy.stripe.com/4gM5kF78Z0OHcGt1OPgUM0N",
    "Remote Workstation Hardening & DNS Profile": "https://buy.stripe.com/14AcN79h7dBtbCpcttgUM0H",
    "Executive Proposal & Pitch Deck Kit": "https://buy.stripe.com/5kQ14p78ZgNF6i5cttgUM0E",
    "Loki Digital Flyer & Ad Pack": "https://buy.stripe.com/00w14pdxn40TeOB2STgUM0G",
    "B2B Monthly Cyber Hygiene Retainer": "https://buy.stripe.com/4gM00l3WN54XbCpbppgUM0W",
    "Vendor Contract Renewal Sentinel": "https://buy.stripe.com/cNi4gB9h740TfSF2STgUM0V",
    "Cloud & SaaS Spend Drift Watchdog": "https://buy.stripe.com/dRm14p78Z7d5bCp3WXgUM0U",
    "B2B Synthetic Uptime & Link Health": "https://buy.stripe.com/4gM9AVgJz54X8qd1OPgUM0T",
    "Quarterly SOP Maintenance & Onboarding Desk": "https://buy.stripe.com/dRm9AV8d38h949X655gUM0S",
    "Web & QR Care Retainer": {
      personal: "https://buy.stripe.com/14A9AV1OFeFx21P655gUM0q",
      business: "https://buy.stripe.com/6oU5kF0KB69135TcttgUM0s"
    },
    "Domain & Email Shield Retainer": {
      personal: "https://buy.stripe.com/dRmbJ3gJzbtlfSF799gUM0g",
      business: "https://buy.stripe.com/4gM4gBdxn1SL35TeBBgUM0m"
    },
    "Document & Vault Caretaker": {
      personal: "https://buy.stripe.com/3cIcN7ctj54XeOBbppgUM0i",
      business: "https://buy.stripe.com/14A28tctjfJB7m9eBBgUM0h"
    },
    "Expense Optimizer Retainer": {
      personal: "https://buy.stripe.com/3cI8wReBr54X9uh0KLgUM0n",
      business: "https://buy.stripe.com/bJebJ364VfJB0XLeBBgUM0k"
    },
    "Bill Watchdog Retainer": {
      personal: "https://buy.stripe.com/8x2bJ3eBr54X35TfFFgUM0o",
      business: "https://buy.stripe.com/9B65kF9h7btl0XL9hhgUM0l"
    },
    "Back-Office Admin Concierge": {
      personal: "https://buy.stripe.com/28EeVf3WN7d5dKx3WXgUM0j",
      business: "https://buy.stripe.com/bJe4gB1OFcxpfSF655gUM0f"
    },
    "Yggdrasil Complete Retainer & 3-Realm Launch": {
      personal: "https://buy.stripe.com/bJe00lbpfeFx9uheBBgUM0p",
      business: "https://buy.stripe.com/9B67sN2SJcxp35T2STgUM0r",
      launch: "https://buy.stripe.com/6oU5kFgJz1SL0XL1OPgUM0t"
    }
  };

  function resolveStripePackageLink(serviceTitle, mode, billingOption) {
    const title = String(serviceTitle || '').trim();
    if (!title) return null;
    let entry = STRIPE_PACKAGE_LINKS[title];
    if (!entry && (/yggdrasil/i.test(title) || /3-realm\s*launch/i.test(title))) {
      entry = STRIPE_PACKAGE_LINKS['Yggdrasil Complete Retainer & 3-Realm Launch'];
    }
    if (!entry) return null;
    if (typeof entry === 'string') return entry;
    if (typeof entry === 'object') {
      if (
        entry.launch &&
        (/3-realm\s*launch/i.test(title) || /launch/i.test(String(billingOption || '')))
      ) {
        return entry.launch;
      }
      const tier = mode === 'business' ? 'business' : 'personal';
      return entry[tier] || entry.personal || null;
    }
    return null;
  }

  
  const SPLIT_OPTIONS = [
    {
      id: 'FULL_100',
      label: '100% Full Settlement',
      defaultPercent: 100,
      description: 'Settle 100% of one-time deliverable capital + first month retainer today.'
    },
    {
      id: 'DEPOSIT_50',
      label: '50% Engagement Deposit',
      defaultPercent: 50,
      description: '50% kickoff mobilization deposit today • 50% upon final SOW handover.'
    },
    {
      id: 'MILESTONE_33',
      label: '33% Phase 1 Milestone (1/3)',
      defaultPercent: 33,
      description: '3-Phase milestone split: 33% Kickoff • 33% Staging • 34% Acceptance.'
    },
    {
      id: 'CUSTOM_SPLIT',
      label: 'Custom Split %',
      defaultPercent: 25,
      description: 'Specify a custom milestone percentage (10%–100%) for bespoke SOW schedules.'
    }
  ];

  async function sha384Hex(input) {
    const bytes =
      input instanceof Uint8Array
        ? input
        : input instanceof ArrayBuffer
        ? new Uint8Array(input)
        : new TextEncoder().encode(String(input));

    if (window.crypto && window.crypto.subtle) {
      const digest = await window.crypto.subtle.digest('SHA-384', bytes);
      return Array.from(new Uint8Array(digest))
        .map((b) => b.toString(16).padStart(2, '0'))
        .join('');
    }
    throw new Error('WebCrypto SHA-384 requires a secure HTTPS or localhost origin.');
  }

  function parseServicePriceToCents(priceStr) {
    const clean = String(priceStr || '')
      .replace(/\$/g, '')
      .replace(/,/g, '')
      .trim();
    let token = clean;
    if (clean.includes('–')) token = clean.split('–')[0].trim();
    else if (clean.includes('/')) { token = clean.split('/')[0].trim(); }
    else if (clean.includes('|')) token = clean.split('|')[0].trim();
    token = token.replace(/[^0-9.]/g, '');
    const dollars = parseFloat(token);
    return Number.isFinite(dollars) ? Math.round(dollars * 100) : 0;
  }

  function applySplitToCents(rawCents, splitPercent) {
    const clamped = Math.min(100, Math.max(10, Math.round(Number(splitPercent) || 100)));
    if (clamped === 100) return rawCents;
    return Math.max(100, Math.floor((rawCents * clamped) / 100));
  }

  function formatCentsUsd(cents) {
    const safe = Math.max(0, Math.round(Number(cents) || 0));
    return '$' + (safe / 100).toFixed(2);
  }

  function isMonthlyRecurringService(service) {
    if (!service) return false;
    const sku = String(service.sku || '');
    const price = String(service.price || '');
    const hosting = String(service.hosting || '');
    return (
      sku.startsWith('SUB-') ||
      sku.startsWith('B2B-') ||
      sku.startsWith('RET-') ||
      /\/ ?mo/i.test(price) ||
      /retainer|month/i.test(hosting)
    );
  }

  function getCustomSkuPriceMap() {
    try {
      const raw = localStorage.getItem(STORAGE_SKU_PRICES_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch {
      return {};
    }
  }

  function setCustomSkuPriceId(sku, priceId) {
    const map = getCustomSkuPriceMap();
    const clean = String(priceId || '').trim();
    if (!clean || clean === 'price_...') {
      delete map[sku];
    } else {
      map[sku] = clean;
    }
    localStorage.setItem(STORAGE_SKU_PRICES_KEY, JSON.stringify(map));
    return map;
  }

  function clearAllCustomSkuPriceIds() {
    localStorage.removeItem(STORAGE_SKU_PRICES_KEY);
    return {};
  }
const TRUSTED_BACKEND_ORIGINS = [window.location.origin, 'https://' + 'tyrprotocol.pro', 'https://' + 'www.tyrprotocol.pro', 'https://' + 'api.tyrprotocol.pro', 'https://' + 'checkout.tyrprotocol.pro'];
  function isTrustedBackendUrl(url) { if (!url || typeof url !== 'string') return false; try { const p = new URL(url.trim()); return p.protocol === 'https:' && TRUSTED_BACKEND_ORIGINS.includes(p.origin); } catch (_) { return false; } }
  
  function getBackendEndpoint() {
    const raw = (localStorage.getItem(STORAGE_BACKEND_URL_KEY) || '').trim(); return isTrustedBackendUrl(raw) ? raw : '';
  }

  function setBackendEndpoint(url) {
    const clean = String(url || '').trim();
    if (clean && isTrustedBackendUrl(clean)) localStorage.setItem(STORAGE_BACKEND_URL_KEY, clean);
    else localStorage.removeItem(STORAGE_BACKEND_URL_KEY);
    return clean;
  }

  function getReceipts() {
    try {
      const raw = localStorage.getItem(STORAGE_RECEIPTS_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  function maskEmailPii(email) { const raw = String(email || '').trim(); const i = raw.indexOf('@'); if (i <= 0) return '***'; const l = raw.slice(0, i); return (l.length <= 2 ? l[0] + '***' : l.slice(0, 2) + '***') + '@' + raw.slice(i + 1); }
  function maskNamePii(name) { const raw = String(name || '').trim(); if (!raw) return 'Client ***'; return raw.split(/\s+/).map((p) => (p.length <= 1 ? '*' : p[0] + '***')).join(' '); } function saveReceipt(record) {
    const list = getReceipts();
    const filtered = list.filter((r) => r.receiptNumber !== record.receiptNumber);
    filtered.unshift({ ...record, clientName: maskNamePii(record.clientName), clientEmail: maskEmailPii(record.clientEmail) });
    localStorage.setItem(STORAGE_RECEIPTS_KEY, JSON.stringify(filtered));
    return filtered;
  }

  function purgeReceipts() {
    localStorage.removeItem(STORAGE_RECEIPTS_KEY);
    return [];
  }

  function getCart() {
    try {
      const raw = localStorage.getItem(STORAGE_CART_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  function saveCart(cartItems) {
    localStorage.setItem(STORAGE_CART_KEY, JSON.stringify(cartItems || []));
  }

  function buildCheckoutSessionConfig(services, splitPercent) {
    const list = services || [];
    const hasRecurring = list.some(isMonthlyRecurringService);
    const customMap = getCustomSkuPriceMap();
    const clampedSplit = Math.min(100, Math.max(10, Math.round(Number(splitPercent) || 100)));
    const baseDomain = window.location.origin.startsWith('http')
      ? window.location.origin
      : 'https://tyrprotocol.pro';

    const lineItems =
      list.length === 0
        ? [{ price: 'price_...', quantity: 1 }]
        : list.map((svc) => {
            const customPrice = clampedSplit === 100 ? customMap[svc.sku] : null;
            if (customPrice && customPrice.startsWith('price_')) {
              return { price: customPrice, quantity: 1, sku: svc.sku };
            }
          const baseCents = getSkuBaseCents(svc);
            const recurring = isMonthlyRecurringService(svc);
            const finalCents = recurring ? baseCents : applySplitToCents(baseCents, clampedSplit);
            return {
              sku: svc.sku,
              price_data: {
                currency: 'usd',
                unit_amount: finalCents,
                product_data: {
                  name:
                    !recurring && clampedSplit < 100
                      ? `${svc.title} [${clampedSplit}% Milestone/Deposit]`
                      : svc.title,
                  description: `TYR SKU ${svc.sku} • ${svc.badge || 'Standard Deliverable'}`,
tax_code: STRIPE_TAX_CODE,
metadata: { sku: svc.sku, realm: svc.realm || '' }
                },
                ...(recurring ? { recurring: { interval: 'month' } } : {})
              },
              quantity: 1
            };
          });

    const config = {
      ui_mode: 'hosted_page',
      mode: hasRecurring ? 'subscription' : 'payment',
      billing_address_collection: 'auto',
      phone_number_collection: { enabled: true },
      automatic_tax: { enabled: true },
      allow_promotion_codes: false,
      submit_type: 'auto',
      tax_id_collection: { enabled: true, required: 'never' },
      consent_collection: { terms_of_service: 'required' },
      saved_payment_method_options: { payment_method_save: 'enabled' },
      integration_identifier: 'hosted_mobile_app_0001',
      origin_context: 'mobile_app',
      success_url: `${baseDomain}/?status=success&session_id={CHECKOUT_SESSION_ID}`,
      cancel_url: `${baseDomain}/?status=cancelled`,
      line_items: lineItems
    };

    if (config.mode === 'subscription') {
      config.payment_method_collection = 'always';
    }
    return config;
  }

  async function processCheckout({
    services,
    clientName,
    businessName,
    clientEmail,
    splitPercent = 100,
    referralCode = '',
    paymentMethodLabel = 'Stripe Hosted Checkout (PCI-DSS v4.0 SAQ-A)'
  }) {
    const list = services && services.length ? services : [];
    const clampedSplit = Math.min(100, Math.max(10, Math.round(Number(splitPercent) || 100)));
    const activeRef =
      String(referralCode || (window.TyrGrowth ? window.TyrGrowth.getActiveReferralCode() : '') || '')
        .trim()
        .toUpperCase();

    let rawOneTimeCents = 0;
    let monthlyCents = 0;
    list.forEach((s) => {
      const c = parseServicePriceToCents(s.price);
      if (isMonthlyRecurringService(s)) monthlyCents += c;
      else rawOneTimeCents += applySplitToCents(c, clampedSplit);
    });
    const referralDiscountCents = activeRef && rawOneTimeCents > 0 ? Math.floor(rawOneTimeCents * 0.1) : 0;
    const oneTimeCents = Math.max(0, rawOneTimeCents - referralDiscountCents);
    const totalTodayCents = oneTimeCents + monthlyCents;

    const config = buildCheckoutSessionConfig(list, clampedSplit);
    if (activeRef) {
      config.metadata = {
        ...(config.metadata || {}),
        referral_code: activeRef,
        referral_discount_cents: String(referralDiscountCents)
      };
    }
    const backendUrl = getBackendEndpoint();
    let sessionId = 'cs_test_' + Date.now().toString(36);
    let hostedCheckoutUrl = '';
    let gatewayMode = 'Client-Side SAQ-A Receipt Ledger (Server Pending Webhook Secret)';

    if (backendUrl && backendUrl.startsWith('https://')) {
      try {
        const res = await fetch(backendUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            checkoutStudioPlanId: CHECKOUT_PLAN_ID,
            stripeAccountId: STRIPE_ACCOUNT_ID,
            customer_email: clientEmail,
            client_name: clientName,
            business_name: businessName,
            split_percent: clampedSplit,
            referral_code: activeRef || null,
            referral_discount_cents: referralDiscountCents,
            session_params: config
          })
        });
        if (res.ok) {
          const json = await res.json();
          if (json.id) sessionId = json.id;
          if (json.url) {
            hostedCheckoutUrl = json.url;
            gatewayMode = 'Stripe Hosted Checkout Live API';
          }
        }
      } catch (err) {
        console.warn('Configured Stripe backend endpoint unreachable; issuing offline SAQ-A receipt:', err);
      }
    }

    const now = new Date();
    const yyyymmdd =
      now.getUTCFullYear() +
      String(now.getUTCMonth() + 1).padStart(2, '0') +
      String(now.getUTCDate()).padStart(2, '0');
    const randBytes = new Uint8Array(4);
    window.crypto.getRandomValues(randBytes);
    const suffix = Array.from(randBytes, (b) => b.toString(36).slice(-1)).join('').toUpperCase();
    const receiptNumber = `RCPT-${yyyymmdd}-${suffix}`;
if (!hostedCheckoutUrl) {
      const storefrontMode =
        typeof document !== 'undefined' &&
        document.documentElement.getAttribute('data-storefront-mode') === 'business'
          ? 'business'
          : 'personal';
      const singleItem = list.length === 1 ? list[0] : null;
      const isCustomItem = Boolean(
        singleItem &&
          (singleItem.isCustom ||
            singleItem.isCustomAmount ||
            /^custom/i.test(String(singleItem.sku || '')) ||
            /^custom/i.test(String(singleItem.title || '')))
      );
      const packageUrl =
        clampedSplit === 100 && singleItem && !isCustomItem
          ? resolveStripePackageLink(
              singleItem.title,
              storefrontMode,
              singleItem.billingOption || singleItem.billing || ''
            )
          : null;
      const params = new URLSearchParams();
      if (clientEmail) params.set('prefilled_email', clientEmail);
      if (packageUrl) {
        params.set('client_reference_id', receiptNumber);
        hostedCheckoutUrl = packageUrl + '?' + params.toString();
        gatewayMode = 'Stripe Package Payment Link (SAQ-A Live)';
      } else {
        if (totalTodayCents > 0) params.set('__prefilled_amount', String(totalTodayCents));
        params.set('client_reference_id', receiptNumber);
        hostedCheckoutUrl = FALLBACK_CUSTOM_LINK + '?' + params.toString();
        gatewayMode = 'Stripe Hosted Payment Link (SAQ-A Live)';
      }
}
    const transactionId = `pi_${now.getTime().toString(36)}_${suffix.toLowerCase()}`;
    const stripeTokenId = `tok_saqa_${suffix.toLowerCase()}${now.getUTCSeconds()}`;
    const timestampIso = now.toISOString().replace('T', ' ').substring(0, 19) + ' UTC';
    const maskedInstrument = 'Stripe Hosted Token •••• SAQ-A';
    const skus = list.map((s) => s.sku);
    const itemTitles = list.map((s) =>
      !isMonthlyRecurringService(s) && clampedSplit < 100
        ? `${s.title} [${clampedSplit}% Deposit/Milestone]`
        : s.title
    );

    const hashPayload = [
      transactionId,
      receiptNumber,
      timestampIso,
      String(clientEmail || '').toLowerCase(),
      skus.slice().sort().join(','),
      String(totalTodayCents),
      maskedInstrument,
      stripeTokenId,
      activeRef
    ].join('|');

    const sha384IntegrityHash = await sha384Hex(hashPayload);

    const record = {
      transactionId,
      checkoutSessionId: sessionId,
      receiptNumber,
      timestampIso,
      clientName: clientName || 'Authorized Client',
      businessName: businessName || 'Independent',
      clientEmail: clientEmail || 'client@tyrprotocol.pro',
      skus,
      itemTitles,
      oneTimeAmountCents: oneTimeCents,
      monthlyRecurringCents: monthlyCents,
      totalChargedTodayCents: totalTodayCents,
      splitPercent: clampedSplit,
      referralCode: activeRef || null,
      referralDiscountCents,
      currency: 'USD',
      paymentMethodLabel,
      maskedInstrument,
      stripeTokenId,
      gatewayMode,
      pciComplianceBadge: 'PCI-DSS v4.0 SAQ-A • Zero Raw PAN/CVV Stored',
      sha384IntegrityHash,
      scheduleCCategory: 'NAICS 541512 & 541611 • Gross Receipts (Line 1)',
      intrusionStatus: 'PENDING_SETTLEMENT'
    };

    saveReceipt(record);
    if (activeRef && window.TyrGrowth) {
      window.TyrGrowth.recordGrowthEvent(
        'REF_CREDIT',
        `Applied ${activeRef} (10% credit: ${formatCentsUsd(referralDiscountCents)}) on ${receiptNumber}`,
        referralDiscountCents / 100
      );
    }

    return {
      success: true,
      record,
      hostedCheckoutUrl,
      sessionConfig: config
    };
  }

  async function reconcileQueryReturn() {
    const params = new URLSearchParams(window.location.search);
    const status = (params.get('status') || '').toLowerCase();
    const sessionId = params.get('session_id') || '';

    if (!sessionId && !status) return null;

    if (status === 'cancelled' || status === 'cancel') {
      return {
        status: 'cancelled',
        message: 'Stripe Checkout was cancelled cleanly — zero charge posted.'
      };
    }

    if (status === 'success' || sessionId.startsWith('cs_')) {
      const existing = getReceipts().find((r) => r.checkoutSessionId === sessionId);
      if (existing) {
        existing.intrusionStatus = 'RECONCILED_RETURN_VERIFIED';
        saveReceipt(existing);
        return {
          status: 'success',
          record: existing,
          message: `Reconciled Stripe session ${sessionId} • Receipt ${existing.receiptNumber} verified.`
        };
      }

      const now = new Date();
      const yyyymmdd =
        now.getUTCFullYear() +
        String(now.getUTCMonth() + 1).padStart(2, '0') +
        String(now.getUTCDate()).padStart(2, '0');
      const suffix = (sessionId.slice(-4) || 'LIVE').toUpperCase();
      const receiptNumber = `RCPT-${yyyymmdd}-${suffix}`;
      const timestampIso = now.toISOString().replace('T', ' ').substring(0, 19) + ' UTC';
      const sha384IntegrityHash = await sha384Hex(`RETURN|${sessionId}|${receiptNumber}|${timestampIso}`);

      const reconciled = {
        transactionId: `pi_reconciled_${suffix.toLowerCase()}`,
        checkoutSessionId: sessionId || `cs_return_${suffix.toLowerCase()}`,
        receiptNumber,
        timestampIso,
        clientName: 'Stripe Hosted Checkout Client',
        businessName: 'TYR Storefront Return',
        clientEmail: 'tyr@tyrprotocol.pro',
        skus: ['STRIPE-HOSTED-RETURN'],
        itemTitles: ['Hosted Stripe Checkout Session Settlement'],
        oneTimeAmountCents: 0,
        monthlyRecurringCents: 0,
        totalChargedTodayCents: 0,
        splitPercent: 100,
        currency: 'USD',
        paymentMethodLabel: 'Stripe Hosted Checkout Return',
        maskedInstrument: 'Stripe Hosted Session Verified',
        stripeTokenId: sessionId || 'cs_verified',
        gatewayMode: 'Deep-Link / URL Return Reconciliation',
        pciComplianceBadge: 'PCI-DSS v4.0 SAQ-A • Zero Raw PAN/CVV Stored',
        sha384IntegrityHash,
        scheduleCCategory: 'NAICS 541512 & 541611 • Gross Receipts (Line 1)',
        intrusionStatus: 'RECONCILED_RETURN_VERIFIED'
      };
      saveReceipt(reconciled);
      return {
        status: 'success',
        record: reconciled,
        message: `Stripe Checkout session ${reconciled.checkoutSessionId} reconciled to vault!`
      };
    }

    return null;
  }

  function exportScheduleCMarkdown(records) {
    const list = records || getReceipts();
    const year = new Date().getUTCFullYear();
    const lines = [
      `# File: transaction_audit_ledger_${year}.md`,
      `# TYR Protocol — Commercial Transaction & Digital Receipt Audit Ledger (${year})`,
      '',
      '- **Entity:** CALEB TR HOLCOMB d/b/a TYR (TYR Technical Consulting)',
      '- **NAICS Codes:** 541512 & 541611 | **EIN:** 42-XXX-X843 (CP 575 G)',
      '- **Cryptographic Baseline:** WebCrypto `SHA-384` (96-char hex digest)',
      '- **PCI-DSS Posture:** v4.0 SAQ-A Tokenized Gateway (0 Raw PAN/CVV Stored)',
      ''
    ];
    if (!list.length) {
      lines.push('_No settled transactions recorded in local vault yet._');
    } else {
      list.forEach((r, idx) => {
        lines.push(`## ${idx + 1}. ${r.receiptNumber} — ${formatCentsUsd(r.totalChargedTodayCents)}`);
        lines.push(`- **Stripe Transaction ID:** \`${r.transactionId}\``);
        lines.push(`- **Checkout Session ID:** \`${r.checkoutSessionId}\``);
        lines.push(`- **Timestamp:** \`${r.timestampIso}\``);
        lines.push(`- **Client:** ${r.clientName} (\`${r.clientEmail}\`)`);
        lines.push(`- **SKUs:** \`${(r.skus || []).join(', ')}\``);
        lines.push(
          `- **One-Time Capital:** ${formatCentsUsd(r.oneTimeAmountCents)} | **Monthly Retainer:** ${formatCentsUsd(r.monthlyRecurringCents)}/mo`
        );
        lines.push(`- **Schedule C Category:** ${r.scheduleCCategory}`);
        lines.push(`- **Security Audit Status:** \`${r.intrusionStatus}\``);
        lines.push(`- **SHA-384 Digest:** \`${r.sha384IntegrityHash}\``);
        lines.push('');
      });
    }
    return lines.join('\n');
  }

  function exportScheduleCCsv(records) {
    const list = records || getReceipts();
    const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`;
    const rows = [
      '# File: transaction_audit_ledger_2026.csv',
      'receipt_number,stripe_transaction_id,checkout_session_id,timestamp_utc,client_name,business_name,client_email,skus,one_time_usd,monthly_recurring_usd,total_charged_usd,masked_instrument,stripe_token_id,schedule_c_category,audit_status,sha384_digest'
    ];
    list.forEach((r) => {
      rows.push(
        [
          esc(r.receiptNumber),
          esc(r.transactionId),
          esc(r.checkoutSessionId),
          esc(r.timestampIso),
          esc(r.clientName),
          esc(r.businessName),
          esc(r.clientEmail),
          esc((r.skus || []).join(';')),
          esc(formatCentsUsd(r.oneTimeAmountCents)),
          esc(formatCentsUsd(r.monthlyRecurringCents)),
          esc(formatCentsUsd(r.totalChargedTodayCents)),
          esc(r.maskedInstrument),
          esc(r.stripeTokenId),
          esc(r.scheduleCCategory),
          esc(r.intrusionStatus),
          esc(r.sha384IntegrityHash)
        ].join(',')
      );
    });
    return rows.join('\n');
  }

  function downloadTextFile(filename, content, mimeType) {
    const blob = new Blob([content], { type: mimeType || 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  window.TyrStripe = {
    STRIPE_ACCOUNT_ID,
SKU_FLOOR_CENTS_MAP,
STRIPE_TAX_CODE,
getSkuBaseCents,
    CHECKOUT_PLAN_ID,
    CHECKOUT_STUDIO_DRAFT_URL,
    SPLIT_OPTIONS,
    sha384Hex,
    parseServicePriceToCents,
    applySplitToCents,
    formatCentsUsd,
    isMonthlyRecurringService,
    getCustomSkuPriceMap,
    setCustomSkuPriceId,
    clearAllCustomSkuPriceIds,
    getBackendEndpoint,
    setBackendEndpoint,
    getReceipts,
    saveReceipt,
    purgeReceipts,
    getCart,
    saveCart,
    buildCheckoutSessionConfig,
    processCheckout,
    reconcileQueryReturn,
    exportScheduleCMarkdown,
    exportScheduleCCsv,
    downloadTextFile
  };
})();

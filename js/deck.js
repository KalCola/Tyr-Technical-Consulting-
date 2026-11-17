// File: js/deck.js
/**
 * TYR Protocol — Upgraded Web Storefront Controller (v4.0)
 * Supports 21 pages, 31 SKUs, Compact Mode, Category Drawer + Recent Tabs,
 * Stripe Checkout Split Calculator & Price ID Mapper, and Field Forensics.
 */
(function () {
  'use strict';

  const DEFAULT_REMOTE_ENDPOINT =
    'https://raw.githubusercontent.com/KalCola/Tyr-Technical-Consulting-/main/data/catalog.json';
  const LOCAL_DYNAMIC_ENDPOINT = './data/catalog.json';

  let catalogData = null;
  let currentPage = 1;
  let activeCardIndex = 0;
  let touchStartX = 0;
  let touchStartY = 0;
  let currentSourceType = 'local';
  let currentSourceUrl = '';

  let compactMode = localStorage.getItem('tyr_compact_mode_v1') === 'true';
  let storefrontMode = localStorage.getItem('tyr_storefront_mode_v1') === 'business' ? 'business' : 'personal';
  let cardTriadKey = 'tyr';
  let cardQrMode = 'url';
  let allRealmsSearch = '';
  let allRealmsCategory = 'ALL';
  let stripeSubTab = 'RECEIPTS';
  let forensicsSubTab = 'HASHER';
  let growthSubTab = 'QUIZ';
  let quizAnswers = { q_web: 0, q_email: 0, q_legal: 0, q_bills: 0 };
  let pitchChannel = 'NEXTDOOR';
  let pitchSku = 'SVC-WEB-01';
  let pitchIncludeRef = true;
  let flyerMode = 'FLYER';
  let flyerTargetType = 'STOREFRONT';
  let flyerSku = 'SVC-WEB-01';
  let flyerIncludeRef = true;
  let highlightedSku = '';
  let chainedWorkOrder = null;

  // Stripe modal state
  let stripeModalServices = [];
  let selectedSplitId = 'FULL_100';

  const isMobileScreen = window.innerWidth <= 768 || navigator.maxTouchPoints > 0;
  const isAndroidAPK = navigator.userAgent.includes('TyrProtocolAndroidAPK');
  if (isMobileScreen || isAndroidAPK) {
    document.documentElement.setAttribute('data-device', 'mobile');
  }  document.documentElement.setAttribute('data-storefront-mode', storefrontMode);
  

  function escapeHtml(str) {
    return String(str ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function getAllServices() {
    if (!catalogData || !Array.isArray(catalogData.pages)) return [];
    const list = [];
    catalogData.pages.forEach((p) => {
      if (Array.isArray(p.services)) {
        p.services.forEach((s) => list.push({ ...s, pageNum: p.page }));
      }
    });
    return list;
  }

  function getRecentTabs() {
    try {
      const raw = localStorage.getItem('tyr_recent_tabs_v1');
      return raw ? JSON.parse(raw) : [1, 11, 20, 21];
    } catch {
      return [1, 11, 20, 21];
    }
  }

  function recordRecentTab(pageNum) {
    const list = getRecentTabs().filter((n) => n !== pageNum);
    list.unshift(pageNum);
    localStorage.setItem('tyr_recent_tabs_v1', JSON.stringify(list.slice(0, 6)));
  }

  const PAGE_DIRECTORY = [
    { page: 0, title: 'P0: 🪪 Personal & Business 3D Card', group: 'Storefront Realms' },
    { page: 1, title: 'P1: Web & Shield', group: 'Storefront Realms' },
    { page: 2, title: 'P2: Contracts & Dossiers', group: 'Storefront Realms' },
    { page: 3, title: 'P3: Budgets & Disputes', group: 'Storefront Realms' },
    { page: 4, title: 'P4: Tactical Kits', group: 'Storefront Realms' },
    { page: 7, title: 'P7: Remote Deliverables', group: 'Storefront Realms' },
    { page: 11, title: 'P11: 🌐 All Realms Search', group: 'Storefront Realms' },
    { page: 5, title: 'P5: Tech Retainers', group: 'Retainers & B2B' },
    { page: 6, title: 'P6: Expense & Yggdrasil', group: 'Retainers & B2B' },
    { page: 8, title: 'P8: 🏢 B2B Governance', group: 'Retainers & B2B' },
    { page: 9, title: 'P9: 🔄 B2B Retainers', group: 'Retainers & B2B' },
    { page: 12, title: 'P12: 🧮 Estimate Cart', group: 'Client Workflows & Stripe' },
    { page: 13, title: 'P13: 📝 Client Intake', group: 'Client Workflows & Stripe' },
    { page: 14, title: 'P14: 📋 Work Orders', group: 'Client Workflows & Stripe' },
    { page: 17, title: 'P17: ✍️ Digital E-Sign', group: 'Client Workflows & Stripe' },
    { page: 19, title: 'P19: 📅 Strategy Call', group: 'Client Workflows & Stripe' },
    { page: 20, title: 'P20: 💳 Stripe & Receipts', group: 'Client Workflows & Stripe' },
    { page: 10, title: 'P10: Bifröst Workflow', group: 'Field Forensics & Security' },
    { page: 15, title: 'P15: 🎨 Image Forge', group: 'Field Forensics & Security' },
    { page: 16, title: 'P16: 📡 Chat Relay', group: 'Field Forensics & Security' },
    { page: 18, title: 'P18: 🛡️ Zero-Custody Vault', group: 'Field Forensics & Security' },
    { page: 21, title: 'P21: 🔬 Field Forensics', group: 'Field Forensics & Security' },
    { page: 22, title: 'P22: 🚀 Growth & Self-Audit', group: 'Client Workflows & Stripe' }
  ];

  function updateModeSwitcherUi() {
    const pBtn = document.getElementById('mode-btn-personal');
    const bBtn = document.getElementById('mode-btn-business');
    if (pBtn) pBtn.className = `mode-switch-pill ${storefrontMode === 'personal' ? 'active-personal' : ''}`;
    if (bBtn) bBtn.className = `mode-switch-pill ${storefrontMode === 'business' ? 'active-business' : ''}`;
  }

  window.setStorefrontMode = function (mode) {
    storefrontMode = mode === 'business' ? 'business' : 'personal';
    localStorage.setItem('tyr_storefront_mode_v1', storefrontMode); 
    document.documentElement.setAttribute('data-storefront-mode', storefrontMode);
    const brandLogo = document.getElementById('header-brand-logo');
    if (brandLogo) brandLogo.src = storefrontMode === 'personal' ? './assets/patron-triad-emblem.svg' : './assets/logo-240.webp';
    updateModeSwitcherUi();
    if (currentPage === 0) {
      const cardEl = document.getElementById('omni-embed-3d');
      if (cardEl) {
        cardEl.dataset.ry = storefrontMode === 'business' ? '180' : '0';
        cardEl.dataset.rx = '3';
        cardEl.style.transform = `rotateX(3deg) rotateY(${cardEl.dataset.ry}deg)`;
        return;
      }
    }
    renderPage(currentPage);
  };

  function updateDockAndCompactUi() {
    const cart = window.TyrStripe.getCart();
    const badge = document.getElementById('dock-cart-badge');
    if (badge) {
      if (cart.length > 0) {
        badge.textContent = String(cart.length);
        badge.style.display = 'inline-block';
      } else {
        badge.style.display = 'none';
      }
    }

    const compactBtn = document.getElementById('compact-mode-toggle');
    if (compactBtn) {
      compactBtn.classList.toggle('active', compactMode);
      compactBtn.textContent = compactMode ? '▤ Compact: ON' : '▤ Compact: OFF';
    }

    updateModeSwitcherUi();
    document.getElementById('dock-btn-card')?.classList.toggle('active', currentPage === 0);
    document.getElementById('dock-btn-catalog')?.classList.toggle('active', currentPage >= 1 && currentPage <= 11);
    document.getElementById('dock-btn-quotes')?.classList.toggle('active', currentPage === 12);
    document.getElementById('dock-btn-stripe')?.classList.toggle('active', currentPage === 20);
    document.getElementById('dock-btn-forensics')?.classList.toggle('active', currentPage === 21);
  }

  window.toggleCompactMode = function () {
    compactMode = !compactMode;
    localStorage.setItem('tyr_compact_mode_v1', String(compactMode));
    updateDockAndCompactUi();
    renderPage(currentPage);
  };

  window.toggleCartSku = function (sku) {
    const all = getAllServices();
    const svc = all.find((s) => s.sku === sku);
    if (!svc) return;
    let cart = window.TyrStripe.getCart();
    if (cart.some((c) => c.sku === sku)) {
      cart = cart.filter((c) => c.sku !== sku);
    } else {
      cart.push(svc);
    }
    window.TyrStripe.saveCart(cart);
    updateDockAndCompactUi();
    renderPage(currentPage);
  };

  async function loadCatalog(overrideUrl = null) {
    const urlParams = new URLSearchParams(window.location.search);
    const paramUrl = urlParams.get('catalog_url') || urlParams.get('endpoint');
    const storedUrl = localStorage.getItem('tyr_catalog_endpoint');

    const targetUrl = overrideUrl || paramUrl || storedUrl || LOCAL_DYNAMIC_ENDPOINT;
    let fetched = false;

    async function tryFetch(url) {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 4500);
      const res = await fetch(url, { cache: 'no-cache', signal: controller.signal });
      clearTimeout(timeoutId);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    }

    try {
      catalogData = await tryFetch(targetUrl);
      currentSourceUrl = targetUrl;
      currentSourceType = targetUrl.startsWith('http') ? 'remote' : 'dynamic_local';
      fetched = true;
    } catch (err) {
      console.warn(`Primary catalog fetch failed for ${targetUrl}:`, err);
      if (targetUrl !== LOCAL_DYNAMIC_ENDPOINT) {
        try {
          catalogData = await tryFetch(`${LOCAL_DYNAMIC_ENDPOINT}?_t=${Date.now()}`);
          currentSourceUrl = LOCAL_DYNAMIC_ENDPOINT;
          currentSourceType = 'dynamic_local';
          fetched = true;
        } catch (localErr) {
          console.warn('Dynamic local fetch failed:', localErr);
        }
      }
    }

    if (!fetched) {
      try {
        catalogData = await tryFetch(DEFAULT_REMOTE_ENDPOINT);
        currentSourceUrl = DEFAULT_REMOTE_ENDPOINT;
        currentSourceType = 'remote';
        fetched = true;
      } catch (remoteErr) {
        console.error('All catalog fetch attempts failed:', remoteErr);
      }
    }

    updateCatalogSourceBadge();
    setupStepperTabs();
    setupTouchSwipe();
    updateDockAndCompactUi();

    try {
      const params = new URLSearchParams(window.location.search);
      const refParam = (params.get('ref') || '').trim();
      const skuParam = (params.get('sku') || '').trim().toUpperCase();
      const modeParam = (params.get('mode') || params.get('face') || '').trim().toLowerCase();
      const pageParam = parseInt(params.get('page') || '', 10);

      if (modeParam === 'personal' || modeParam === 'business') {
        storefrontMode = modeParam;
        localStorage.setItem('tyr_storefront_mode_v1', storefrontMode);
        updateModeSwitcherUi();
      }

      if (refParam && window.TyrGrowth) {
        const appliedRef = window.TyrGrowth.setActiveReferralCode(refParam);
        window.TyrGrowth.recordGrowthEvent('REF_VISIT', `Inbound referral link activated: ${appliedRef}`);
        const banner = document.getElementById('return-reconcile-banner');
        const text = document.getElementById('return-reconcile-text');
        if (banner && text) {
          text.textContent = `Referral Code ${appliedRef} active — 10% Welcome Credit unlocked on one-time builds!`;
          banner.style.display = 'flex';
        }
      }

      if (skuParam) {
        const matchSvc = getAllServices().find((s) => s.sku === skuParam);
        if (matchSvc) {
          highlightedSku = matchSvc.sku;
          currentPage = matchSvc.pageNum || 1;
          const pageObj = catalogData?.pages?.find((p) => p.page === currentPage);
          const idx = (pageObj?.services || []).findIndex((s) => s.sku === skuParam);
          if (idx >= 0) activeCardIndex = idx;
          pitchSku = matchSvc.sku;
          flyerSku = matchSvc.sku;
        }
      } else if (!Number.isNaN(pageParam) && pageParam >= 0 && pageParam <= 22) {
        currentPage = pageParam;
      }

      const returnRes = await window.TyrStripe.reconcileQueryReturn();
      if (returnRes) {
        const banner = document.getElementById('return-reconcile-banner');
        const text = document.getElementById('return-reconcile-text');
        if (banner && text) {
          text.textContent = returnRes.message;
          banner.style.display = 'flex';
        }
        window.history.replaceState({}, document.title, window.location.pathname);
        if (returnRes.status === 'success') {
          currentPage = 20;
        }
      }
    } catch (e) {
      console.warn('Session return / deep-link reconcile skipped:', e);
    }

    renderPage(currentPage);
  }

  function updateCatalogSourceBadge() {
    const badge = document.getElementById('catalog-source-badge');
    if (!badge) return;
    if (currentSourceType === 'remote') {
      badge.innerHTML = '🟢 Remote JSON';
      badge.className = 'catalog-source-badge badge-remote';
      badge.title = `Live Remote Endpoint: ${currentSourceUrl}`;
    } else if (currentSourceType === 'dynamic_local') {
      badge.innerHTML = '⚡ Dynamic Local';
      badge.className = 'catalog-source-badge badge-dynamic';
      badge.title = `Local Dynamic Source: ${currentSourceUrl}`;
    } else {
      badge.innerHTML = '📦 Cached Local';
      badge.className = 'catalog-source-badge badge-cached';
    }
    const endpointInput = document.getElementById('custom-endpoint-input');
    if (endpointInput && !endpointInput.value) {
      endpointInput.value = currentSourceUrl;
    }
  }

  window.openCatalogSyncModal = function () {
    const modal = document.getElementById('catalog-sync-modal');
    if (modal) {
      const input = document.getElementById('custom-endpoint-input');
      if (input) input.value = currentSourceUrl || DEFAULT_REMOTE_ENDPOINT;
      modal.style.display = 'flex';
    }
  };

  window.closeCatalogSyncModal = function () {
    const modal = document.getElementById('catalog-sync-modal');
    if (modal) modal.style.display = 'none';
  };

  window.applyCatalogEndpoint = async function (e) {
    if (e) e.preventDefault();
    const input = document.getElementById('custom-endpoint-input');
    const newUrl = input ? input.value.trim() : '';
    if (newUrl) {
      localStorage.setItem('tyr_catalog_endpoint', newUrl);
      await loadCatalog(newUrl);
    } else {
      await window.resetCatalogEndpoint();
    }
    window.closeCatalogSyncModal();
  };

  window.resetCatalogEndpoint = async function () {
    localStorage.removeItem('tyr_catalog_endpoint');
    await loadCatalog(LOCAL_DYNAMIC_ENDPOINT);
    window.closeCatalogSyncModal();
  };

  window.openCategoryDrawer = function () {
    const modal = document.getElementById('category-drawer-modal');
    const recentEl = document.getElementById('drawer-recent-container');
    const groupsEl = document.getElementById('drawer-groups-container');
    if (!modal || !recentEl || !groupsEl) return;

    const recents = getRecentTabs();
    recentEl.innerHTML =
      '<span style="font-size:10px;color:var(--text-gold);font-weight:800;align-self:center;">⏱️ Recent:</span>' +
      recents
        .map((pg) => {
          const entry = PAGE_DIRECTORY.find((d) => d.page === pg);
          if (!entry) return '';
          return `<button class="filter-pill ${pg === currentPage ? 'active' : ''}" onclick="window.navigateFromDrawer(${pg})">${escapeHtml(entry.title)}</button>`;
        })
        .join('');

    const groups = [
      'Storefront Realms',
      'Retainers & B2B',
      'Client Workflows & Stripe',
      'Field Forensics & Security'
    ];
    groupsEl.innerHTML = groups
      .map((g) => {
        const items = PAGE_DIRECTORY.filter((d) => d.group === g);
        return `
          <div class="drawer-group">
            <div class="drawer-group-title">${escapeHtml(g)}</div>
            <div class="drawer-grid">
              ${items
                .map(
                  (d) =>
                    `<button class="drawer-item-btn ${d.page === currentPage ? 'active' : ''}" onclick="window.navigateFromDrawer(${d.page})">${escapeHtml(d.title)}</button>`
                )
                .join('')}
            </div>
          </div>
        `;
      })
      .join('');

    modal.style.display = 'flex';
  };

  window.closeCategoryDrawer = function () {
    const modal = document.getElementById('category-drawer-modal');
    if (modal) modal.style.display = 'none';
  };

  window.navigateFromDrawer = function (pg) {
    window.closeCategoryDrawer();
    window.navigateToPage(pg);
  };

  function setupStepperTabs() {
    document.querySelectorAll('.step-tab[data-page]').forEach((tab) => {
      if (tab.dataset.bound === '1') return;
      tab.dataset.bound = '1';
      tab.addEventListener('click', (e) => {
        e.preventDefault();
        const p = parseInt(tab.getAttribute('data-page'), 10);
        if (!Number.isNaN(p) && p !== currentPage) {
          window.navigateToPage(p);
        }
      });
    });
  }

  window.navigateToPage = function (pageNum) {
    currentPage = pageNum;
    activeCardIndex = 0;
    recordRecentTab(pageNum);
    renderPage(pageNum);
  };

  function isBusinessFocusedService(svc) {
    return /SVC-B2B|RET-|DCK/.test(svc.sku || '') || svc.pageNum === 8 || svc.pageNum === 9;
  }

  function renderServiceCardsIntoGrid(services, deckGrid) {
    const cart = window.TyrStripe.getCart();
    services.forEach((svc, index) => {
      const inCart = cart.some((c) => c.sku === svc.sku);
      const isBizSku = isBusinessFocusedService(svc);
      const modeMatches =
        (storefrontMode === 'business' && isBizSku) ||
        (storefrontMode === 'personal' && !isBizSku);
      const modeClass = modeMatches
        ? storefrontMode === 'business'
          ? 'realm-glow-business'
          : 'realm-glow-personal'
        : '';
      const tierBadge = isBizSku
        ? '<span class="mode-tier-badge tier-business">🏢 Business Tier</span>'
        : '<span class="mode-tier-badge tier-personal">👤 Personal / Solo</span>';

      const card = document.createElement('article');
      card.className = `card ${index === activeCardIndex ? 'mobile-active' : ''} ${compactMode ? 'compact-mode' : ''} ${modeClass}`;
      if (highlightedSku && svc.sku === highlightedSku) {
        card.style.borderColor = '#fbbf24';
        card.style.boxShadow = '0 0 24px rgba(251, 191, 36, 0.25)';
      }
      card.setAttribute('data-sku', svc.sku);

      const bulletsHtml = (svc.bullets || []).map((b) => `<li>${escapeHtml(b)}</li>`).join('');
      const compactBtnHtml = compactMode
        ? `<button type="button" class="btn-secondary" style="padding:4px 8px;font-size:10px;" onclick="this.closest('.card').classList.toggle('expanded'); this.textContent = this.closest('.card').classList.contains('expanded') ? 'Hide ▴' : 'Details ▾';">Details ▾</button>`
        : '';

      card.innerHTML = `
        <div class="card-top">
          <div class="card-top-left">
            <span class="rune-pill">${escapeHtml(svc.rune)}</span>
            <span class="code-tag">${escapeHtml(svc.sku)}</span>
            <span class="realm-meaning">${escapeHtml(svc.realm)}</span>
            ${tierBadge}
          </div>
          <div class="price-box">
            <div class="price-val">${escapeHtml(svc.price)}</div>
          </div>
        </div>
        <div class="card-top-titles">
          <h3>${escapeHtml(svc.title)}</h3>
        </div>
        <div class="attr-strip">
          <div class="attr-item">
            <span class="attr-label">Realm</span>
            <span class="attr-value">${escapeHtml(svc.realmMeaning)}</span>
          </div>
          <div class="attr-item">
            <span class="attr-label">Hosting/Tier</span>
            <span class="attr-value">${escapeHtml(svc.hosting)}</span>
          </div>
          <div class="attr-item">
            <span class="attr-label">Turnaround</span>
            <span class="attr-value">${escapeHtml(svc.turnaround)}</span>
          </div>
        </div>
        <ul class="card-bullets">${bulletsHtml}</ul>
        <div class="card-footer" style="flex-wrap:wrap;gap:6px;">
          <div class="badge-group">
            <span class="badge">${escapeHtml(svc.badge)}</span>
            <span class="spec-tag">${escapeHtml(svc.spec)}</span>
          </div>
          <div style="display:flex;gap:6px;flex-wrap:wrap;align-items:center;">
            ${compactBtnHtml}
            <button type="button" class="btn-secondary" style="padding:6px 8px;font-size:11px;" onclick="window.shareServiceSku('${escapeHtml(svc.sku)}')" title="Share SKU Deep-Link &amp; Pitch">
              ↗ Share
            </button>
            <button type="button" class="btn-secondary" style="padding:6px 9px;font-size:11px;" onclick="window.toggleCartSku('${escapeHtml(svc.sku)}')">
              ${inCart ? '✓ In Quote' : '+ Quote'}
            </button>
            <button type="button" class="btn-stripe" onclick="window.openStripeForSku('${escapeHtml(svc.sku)}')">
              💳 Stripe Pay
            </button>
            <button type="button" class="btn-primary order-trigger" data-sku="${escapeHtml(svc.sku)}" style="padding:6px 10px;font-size:11px;">
              Intake
            </button>
          </div>
        </div>
      `;
      deckGrid.appendChild(card);
    });

    document.querySelectorAll('.order-trigger').forEach((btn) => {
      btn.addEventListener('click', () => {
        window.openClientIntakeModal(btn.getAttribute('data-sku'));
      });
    });
  }

  function renderPage(pageNumber) {
    if (!catalogData) return;

    document.querySelectorAll('.step-tab[data-page]').forEach((tab) => {
      const p = parseInt(tab.getAttribute('data-page'), 10);
      tab.classList.toggle('active', p === pageNumber);
    });

    const pageHeaderTitle = document.getElementById('page-title');
    const pageHeaderSub = document.getElementById('page-subtitle');
    const deckGrid = document.getElementById('deck-grid');
    const mobileCounter = document.getElementById('mobile-card-counter');
    const stepControls = document.getElementById('mobile-step-controls');

    if (!deckGrid) return;
    deckGrid.innerHTML = '';

    const isCatalogStepperPage = pageNumber >= 1 && pageNumber <= 9;
    deckGrid.className = 'deck-grid';
    if (!isCatalogStepperPage) deckGrid.classList.add('tool-view');
    if (compactMode && isCatalogStepperPage) deckGrid.classList.add('compact-grid');
    if (stepControls) {
      stepControls.style.display = isCatalogStepperPage && !compactMode ? '' : 'none';
    }

    updateDockAndCompactUi();

    if (pageNumber === 0) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = '🪪 Caleb Holcomb — Personal & Business 3D Card';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Nine Realms Obsidian dual-face identity • Drag to tilt or switch Personal ↔ Business.';
      renderOmniCardPage(deckGrid);
      return;
    }

    if (pageNumber === 10) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Bifröst 3-Step Workflow & Zero-Custody Covenant';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Engage TYR Technical Consulting with zero friction, zero lock-in, and 0-day statement purge.';
      const wfPage = catalogData.pages.find((p) => p.page === 10 || Array.isArray(p.workflow));
      renderWorkflowPage(wfPage, deckGrid);
      return;
    }
    if (pageNumber === 11) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = '🌐 All Realms Catalog (31 SKUs)';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Search all technical, remote, B2B governance, and monthly retainer SKUs.';
      renderAllRealmsPage(deckGrid);
      return;
    }
    if (pageNumber === 12) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = '🧮 Estimate & Split-Payment Cart';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Bundle SKUs, calculate deposits, or launch Stripe Hosted Checkout.';
      renderCartPage(deckGrid);
      return;
    }
    if (pageNumber === 13) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Client Intake & Work Order Generation';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Direct project onboarding, binding SLAs, and chained SOW + Stripe settlement.';
      renderIntakePage(deckGrid);
      return;
    }
    if (pageNumber === 14) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Active Work Orders Ledger';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Track submitted engagement requests and deliverable timelines.';
      renderTrackerPage(deckGrid);
      return;
    }
    if (pageNumber === 15) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Runic Image Forge: Týr & Freyja Studio';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Synthesize canonical Norse mythology prompts and visual imagery.';
      renderImageForgePage(deckGrid);
      return;
    }
    if (pageNumber === 16) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Google Chat Relay: Zero-Custody Bridge';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Scrubbed inbound brief dispatch via Google Chat API.';
      renderGoogleChatRelayPage(deckGrid);
      return;
    }
    if (pageNumber === 17) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Digital Contract E-Sign (15 U.S.C. § 7001)';
      if (pageHeaderSub) pageHeaderSub.textContent = 'On-device cryptographic execution of SOWs & MSAs with real WebCrypto SHA-384 certification.';
      renderContractSignPage(deckGrid);
      return;
    }
    if (pageNumber === 18) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Zero-Custody Document Vault & Sanitizer';
      if (pageHeaderSub) pageHeaderSub.textContent = '0 Raw Financial Data Stored: Redact sensitive accounts in memory.';
      renderZeroCustodyVaultPage(deckGrid);
      return;
    }
    if (pageNumber === 19) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = 'Bifröst Strategy Call Scheduler';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Book a scoping or architecture review with Caleb Holcomb.';
      renderBookingPage(deckGrid);
      return;
    }
    if (pageNumber === 20) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = '💳 Stripe Checkout & SHA-384 Receipts Hub';
      if (pageHeaderSub) pageHeaderSub.textContent = 'PCI-DSS v4.0 SAQ-A gateway, Price ID mapper, split calculator & IRS Schedule C CSV/MD exports.';
      renderStripeHubPage(deckGrid);
      return;
    }
    if (pageNumber === 21) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = '🔬 Field Forensics & Telemetry Suite';
      if (pageHeaderSub) pageHeaderSub.textContent = 'Local WebCrypto SHA-384 file hasher, zero-upload EXIF/GPS scrubber, and mobile RCA log builder.';
      renderForensicsHubPage(deckGrid);
      return;
    }
    if (pageNumber === 22) {
      if (pageHeaderTitle) pageHeaderTitle.textContent = '🚀 $0 Self-Marketing, Quick Audit & QR Studio';
      if (pageHeaderSub) pageHeaderSub.textContent = '60-second audit quiz, one-tap local pitches, printable QR flyers, NW Alabama case studies, referral credit & GBP checklist.';
      renderGrowthHubPage(deckGrid);
      return;
    }

    const pageObj = catalogData.pages.find((p) => p.page === pageNumber);
    if (!pageObj) return;
    if (pageHeaderTitle) pageHeaderTitle.textContent = pageObj.title;
    if (pageHeaderSub) pageHeaderSub.textContent = pageObj.subtitle;

    const services = pageObj.services || [];
    renderServiceCardsIntoGrid(services, deckGrid);

    if (mobileCounter) {
      mobileCounter.textContent = `Card ${activeCardIndex + 1} of ${services.length}`;
    }
  }

  function renderOmniCardPage(container) {
    const triad = {
      front: {
        tyr: {
          glow: 'glow-tyr',
          accent: '#dc2626',
          btnCls: 'active-tyr',
          attrs: [['Pillar', 'ᛏ Tiwaz Spine'], ['Alignment', 'Pisces • 1997'], ['Region', 'NW Alabama']],
          heading: 'ᛏ TÝR — UNYIELDING INTEGRITY & KEPT OATHS',
          body: 'Direct, plainspoken candor and technical accountability—standing firmly behind every architecture and word without pretense.'
        },
        freyja: {
          glow: 'glow-freyja',
          accent: '#fbbf24',
          btnCls: 'active-freyja',
          attrs: [['Pillar', 'ᚠ Fehu Flame'], ['Boundary', 'Sovereign Agency'], ['Custody', 'Zero-Coercion']],
          heading: 'ᚠ FREYJA — SOVEREIGN BOUNDARIES & SELF-WORTH',
          body: 'Fierce independence and personal privacy—every relationship and collaboration remains voluntary, transparent, and uncoerced.'
        },
        loki: {
          glow: 'glow-loki',
          accent: '#34d399',
          btnCls: 'active-loki',
          attrs: [['Pillar', 'ᚲ Kenaz Wit'], ['Craft', 'Bare-Metal Linux'], ['Ethos', 'Zero Dogma']],
          heading: 'ᚲ LOKI — TACTICAL ADAPTABILITY WITHOUT DOGMA',
          body: 'Relentless curiosity across hardware, Windows/Linux internals, and code—solving hard bottlenecks through lateral ingenuity.'
        }
      },
      back: {
        tyr: {
          glow: 'glow-tyr',
          accent: '#dc2626',
          btnCls: 'active-tyr',
          attrs: [['NAICS', '541512 • 541611'], ['Custody', '0-Day Data Purge'], ['Rates', '$65–$495 Flat']],
          heading: 'ᛏ TÝR QUALITY — ROOT-CAUSE PRECISION & VERIFIABLE RIGOR',
          body: 'Telemetry-first diagnostics and SHA-384 verifiable engineering. Every deliverable is backed by statutory compliance and clean documentation.'
        },
        freyja: {
          glow: 'glow-freyja',
          accent: '#fbbf24',
          btnCls: 'active-freyja',
          attrs: [['Ownership', '100% Client'], ['Billing', 'PCI-DSS SAQ-A'], ['Defense', 'Schedule C / ISP']],
          heading: 'ᚠ FREYJA QUALITY — 100% CLIENT CUSTODY & ZERO LOCK-IN',
          body: 'Complete administrative sovereignty. Clients hold full domain, repo, and asset custody outright—never held hostage by SaaS fees.'
        },
        loki: {
          glow: 'glow-loki',
          accent: '#34d399',
          btnCls: 'active-loki',
          attrs: [['Lock-In', '$0/Mo Hosting'], ['Turnaround', '24–72h Turnkey'], ['Delivery', 'US Remote + NW AL']],
          heading: 'ᚲ LOKI QUALITY — LEAN, LOCAL-FIRST & RESILIENT EXECUTION',
          body: 'Eliminating bloated agency retainers to ship fast, low-overhead, durable systems engineered for everyday Main Street & B2B operators.'
        }
      }
    };

    const fData = triad.front[cardTriadKey] || triad.front.tyr;
    const bData = triad.back[cardTriadKey] || triad.back.tyr;
    const qrPayload =
      cardQrMode === 'url'
        ? `https://tyrprotocol.pro/?mode=${storefrontMode}`
        : window.TyrGrowth
        ? window.TyrGrowth.MECARD_QR_STRING
        : 'https://tyrprotocol.pro';
    const qrSvg = window.TyrGrowth
      ? window.TyrGrowth.renderQrSvg(qrPayload, '#090a0f', '#ffffff', 112)
      : '';
    const qrLabel = cardQrMode === 'url' ? `SCAN: tyrprotocol.pro` : 'SCAN: Contact Card';
    const initialRy = storefrontMode === 'business' ? 180 : 0;

    const stage = document.createElement('div');
    stage.className = 'omni-embed-stage';

    const attrHtml = (attrs, accent) => `
      <div class="attr-strip" style="border-left: 3px solid ${accent};">
        ${attrs
          .map(
            ([k, v]) => `
          <div class="attr-item">
            <span class="attr-label">${escapeHtml(k)}</span>
            <span class="attr-value">${escapeHtml(v)}</span>
          </div>`
          )
          .join('')}
      </div>
    `;

    const deityTabsHtml = (isBack) => `
      <div class="omni-deity-tabs">
        <button type="button" class="omni-deity-btn ${cardTriadKey === 'tyr' ? 'active-tyr' : ''}" onclick="window.selectEmbeddedTriad('tyr')">ᛏ Týr (${isBack ? 'Precision' : 'Truth'})</button>
        <button type="button" class="omni-deity-btn ${cardTriadKey === 'freyja' ? 'active-freyja' : ''}" onclick="window.selectEmbeddedTriad('freyja')">ᚠ Freyja (${isBack ? 'Custody' : 'Sovereign'})</button>
        <button type="button" class="omni-deity-btn ${cardTriadKey === 'loki' ? 'active-loki' : ''}" onclick="window.selectEmbeddedTriad('loki')">ᚲ Loki (${isBack ? 'Ingenuity' : 'Adaptable'})</button>
      </div>
    `;

    stage.innerHTML = `
      <div style="width:100%;max-width:720px;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;">
        <span style="font-size:11px;color:var(--text-gold);font-family:var(--font-mono);">⇄ Drag card to tilt 3D • Tap QR to switch URL ↔ Contact</span>
        <div style="display:flex;gap:6px;flex-wrap:wrap;">
          <button type="button" class="btn-secondary" style="font-size:11px;padding:6px 10px;" onclick="window.flipEmbeddedCard()">⇄ Flip Personal ↔ Business</button>
          <a href="./assets/3d_card.html?mode=${escapeHtml(storefrontMode)}" target="_blank" rel="noopener noreferrer" class="btn-primary" style="font-size:11px;padding:6px 10px;">↗ Fullscreen Card</a>
        </div>
      </div>

      <div class="omni-embed-perspective" id="omni-embed-perspective">
        <div class="omni-embed-3d" id="omni-embed-3d" data-rx="3" data-ry="${initialRy}" style="transform: rotateX(3deg) rotateY(${initialRy}deg);">
          <!-- PERSONAL FACE -->
          <article class="omni-embed-face front-face ${fData.glow}">
            <div class="omni-embed-top">
              <span style="color:#f87171;">ᛏ PERSONAL &bull; TEIWAZ &bull; FEHU &bull; KENAZ</span>
              <div class="mode-switcher">
                <button type="button" class="mode-switch-pill active-personal" onclick="window.setStorefrontMode('personal')">👤 Personal</button>
                <button type="button" class="mode-switch-pill" onclick="window.setStorefrontMode('business')">🏢 Business</button>
              </div>
            </div>

            <div class="omni-embed-grid">
              <div style="display:flex;flex-direction:column;gap:8px;">
                <div style="display:flex;align-items:center;gap:12px;">
                  <div style="width:58px;height:58px;border-radius:10px;border:1.5px solid #dc2626;background:#090a0f;display:flex;align-items:center;justify-content:center;font-size:28px;color:#fbbf24;font-weight:800;flex-shrink:0;box-shadow:0 0 16px rgba(220,38,38,0.32);"<img src="./assets/patron-triad-emblem.svg" alt="Patron Triad Neon Bind-Rune Emblem" style="width:100%;height:100%;object-fit:cover;border-radius:10px;">ᛏ</div>
                  <div>
                    <h3 style="margin:0;font-size:17px;color:#fff;">CALEB TR HOLCOMB</h3>
                    <div style="font-size:12px;font-weight:700;color:#fbbf24;">Principal Systems Engineer &amp; Developer</div>
                    <div style="font-family:var(--font-mono);font-size:10px;color:var(--text-muted);">Teiwaz (ᛏ) Anchored • Patron Triad • NW Alabama</div>
                  </div>
                </div>
                ${attrHtml(fData.attrs, fData.accent)}
                ${deityTabsHtml(false)}
                <div style="background:var(--bg-surface);border:1px solid var(--border-subtle);border-left:3px solid ${fData.accent};border-radius:8px;padding:8px 10px;font-size:11px;color:var(--text-secondary);">
                  <div style="font-weight:800;color:#fbbf24;margin-bottom:2px;">${escapeHtml(fData.heading)}</div>
                  <div>${escapeHtml(fData.body)}</div>
                </div>
              </div>

              <button type="button" class="omni-qr-box" onclick="window.toggleEmbeddedCardQr()" title="Tap QR to toggle Storefront URL vs Offline Contact">
                <div class="omni-qr-frame">${qrSvg}</div>
                <span style="font-family:var(--font-mono);font-size:10px;font-weight:800;color:#fbbf24;margin-top:5px;">${escapeHtml(qrLabel)}</span>
                <span style="font-size:9px;color:var(--text-muted);">Tap QR to switch</span>
              </button>
            </div>

            <div class="card-footer" style="flex-wrap:wrap;gap:6px;padding-top:10px;">
              <div style="display:flex;gap:6px;flex-wrap:wrap;">
                <button type="button" class="btn-primary" style="font-size:11px;padding:6px 10px;background:#059669;" onclick="window.TyrGrowth && window.TyrGrowth.downloadVCard()">⬇ Save .vcf</button>
                <button type="button" class="btn-secondary" style="font-size:11px;padding:6px 10px;" onclick="window.shareEmbeddedCard()">🔗 Share</button>
                <a class="btn-secondary" style="font-size:11px;padding:6px 9px;" href="https://github.com/kalcola" target="_blank" rel="noopener noreferrer">⌘ GitHub</a>
                <a class="btn-secondary" style="font-size:11px;padding:6px 9px;" href="tel:+16592578043">📞 (659) 257-8043</a>
                <a class="btn-secondary" style="font-size:11px;padding:6px 9px;" href="mailto:colacal43@gmail.com">✉ Personal</a>
                <a class="btn-secondary" style="font-size:11px;padding:6px 9px;" href="mailto:tyr@tyrprotocol.pro">✉ Work</a>
              </div>
              <button type="button" class="btn-secondary" style="font-size:11px;padding:6px 10px;border-color:#fbbf24;color:#fbbf24;" onclick="window.setStorefrontMode('business')">⇄ Business Side</button>
            </div>
          </article>

          <!-- BUSINESS FACE -->
          <article class="omni-embed-face back-face ${bData.glow}">
            <div class="omni-embed-top">
              <span style="color:#fbbf24;">🏢 BUSINESS &bull; NAICS 541512 &amp; 541611 &bull; ZERO-CUSTODY</span>
              <div class="mode-switcher">
                <button type="button" class="mode-switch-pill" onclick="window.setStorefrontMode('personal')">👤 Personal</button>
                <button type="button" class="mode-switch-pill active-business" onclick="window.setStorefrontMode('business')">🏢 Business</button>
              </div>
            </div>

            <div class="omni-embed-grid">
              <div style="display:flex;flex-direction:column;gap:8px;">
                <div style="display:flex;align-items:center;gap:12px;">
                  <img src="./assets/logo-240.webp" alt="TYR Technical Consulting Shield" style="width:58px;height:58px;border-radius:10px;border:1.5px solid #fbbf24;object-fit:cover;flex-shrink:0;box-shadow:0 0 16px rgba(251,191,36,0.3);">
                  <div>
                    <h3 style="margin:0;font-size:17px;color:#fff;">TYR TECHNICAL CONSULTING</h3>
                    <div style="font-size:12px;font-weight:700;color:#34d399;">CALEB TR HOLCOMB d/b/a TYR • Verified Practice</div>
                    <div style="font-family:var(--font-mono);font-size:10px;color:var(--text-muted);">EIN: 42-XXX-X843 (CP 575 G) • NAICS 541512 &amp; 541611</div>
                  </div>
                </div>
                ${attrHtml(bData.attrs, bData.accent)}
                ${deityTabsHtml(true)}
                <div style="background:var(--bg-surface);border:1px solid var(--border-subtle);border-left:3px solid ${bData.accent};border-radius:8px;padding:8px 10px;font-size:11px;color:var(--text-secondary);">
                  <div style="font-weight:800;color:#fbbf24;margin-bottom:2px;">${escapeHtml(bData.heading)}</div>
                  <div>${escapeHtml(bData.body)}</div>
                </div>
              </div>

              <button type="button" class="omni-qr-box" onclick="window.toggleEmbeddedCardQr()" title="Tap QR to toggle Storefront URL vs Offline Contact">
                <div class="omni-qr-frame">${qrSvg}</div>
                <span style="font-family:var(--font-mono);font-size:10px;font-weight:800;color:#fbbf24;margin-top:5px;">${escapeHtml(qrLabel)}</span>
                <span style="font-size:9px;color:var(--text-muted);">Tap QR to switch</span>
              </button>
            </div>

            <div class="card-footer" style="flex-wrap:wrap;gap:6px;padding-top:10px;">
              <div style="display:flex;gap:6px;flex-wrap:wrap;">
                <button type="button" class="btn-primary" style="font-size:11px;padding:6px 10px;background:#059669;" onclick="window.TyrGrowth && window.TyrGrowth.downloadVCard()">⬇ Save .vcf</button>
                <a class="btn-secondary" style="font-size:11px;padding:6px 9px;border-color:#fbbf24;color:#fde68a;" href="sms:+16592578043?body=Hi%20Caleb%20(TYR%20Technical%20Consulting)%2C%20here%20is%20my%20contact%20info%3A%20">📲 Send Info</a>
                <button type="button" class="btn-primary" style="font-size:11px;padding:6px 10px;" onclick="navigateToPage(8)">🏢 B2B Suite</button>
                <a class="btn-secondary" style="font-size:11px;padding:6px 9px;" href="https://www.linkedin.com/company/tyrs-technical-consulting" target="_blank" rel="noopener noreferrer">in LinkedIn</a>
                <a class="btn-secondary" style="font-size:11px;padding:6px 9px;" href="mailto:tyr@tyrprotocol.pro">✉ Work</a>
              </div>
              <button type="button" class="btn-secondary" style="font-size:11px;padding:6px 10px;border-color:#dc2626;color:#fca5a5;" onclick="window.setStorefrontMode('personal')">⇄ Personal Side</button>
            </div>
          </article>
        </div>
      </div>

      <div style="width:100%;max-width:720px;display:flex;gap:8px;flex-wrap:wrap;justify-content:center;">
        <button type="button" class="btn-secondary" onclick="navigateToPage(11)">🌐 Browse All 31 SKUs (${storefrontMode === 'business' ? 'Business Focus' : 'Personal Focus'})</button>
        <button type="button" class="btn-stripe" onclick="navigateToPage(22)">🚀 Run 60s Free Audit &amp; Print QR Flyers</button>
      </div>
    `;

    container.appendChild(stage);
    bindEmbeddedCardDrag();
  }

  function bindEmbeddedCardDrag() {
    const persp = document.getElementById('omni-embed-perspective');
    const cardEl = document.getElementById('omni-embed-3d');
    if (!persp || !cardEl || persp.dataset.dragBound === '1') return;
    persp.dataset.dragBound = '1';

    let dragging = false;
    let sx = 0;
    let sy = 0;
    let srx = parseFloat(cardEl.dataset.rx || '3');
    let sry = parseFloat(cardEl.dataset.ry || '0');

    const applyTransform = (rx, ry) => {
      cardEl.dataset.rx = String(rx);
      cardEl.dataset.ry = String(ry);
      cardEl.style.transform = `rotateX(${rx.toFixed(1)}deg) rotateY(${ry.toFixed(1)}deg)`;
    };

    const isInteractive = (t) => Boolean(t && t.closest && t.closest('a, button'));

    persp.addEventListener('mousedown', (e) => {
      if (isInteractive(e.target)) return;
      dragging = true;
      sx = e.clientX;
      sy = e.clientY;
      srx = parseFloat(cardEl.dataset.rx || '3');
      sry = parseFloat(cardEl.dataset.ry || '0');
      persp.classList.add('dragging');
      cardEl.classList.add('no-transition');
    });

    window.addEventListener('mousemove', (e) => {
      if (!dragging) return;
      const ry = sry + (e.clientX - sx) * 0.5;
      const rx = Math.max(-55, Math.min(55, srx - (e.clientY - sy) * 0.4));
      applyTransform(rx, ry);
    });

    window.addEventListener('mouseup', () => {
      if (!dragging) return;
      dragging = false;
      persp.classList.remove('dragging');
      cardEl.classList.remove('no-transition');
    });

    persp.addEventListener('dblclick', (e) => {
      if (!isInteractive(e.target)) window.flipEmbeddedCard();
    });
  }

  window.flipEmbeddedCard = function () {
    window.setStorefrontMode(storefrontMode === 'personal' ? 'business' : 'personal');
  };

  window.selectEmbeddedTriad = function (key) {
    cardTriadKey = key;
    renderPage(0);
  };

  window.toggleEmbeddedCardQr = function () {
    cardQrMode = cardQrMode === 'url' ? 'vcard' : 'url';
    renderPage(0);
  };

  window.shareEmbeddedCard = async function () {
    const shareUrl = `https://tyrprotocol.pro/?page=0&mode=${storefrontMode}`;
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Caleb Holcomb — TYR Personal & Business 3D Card',
          text: 'Nine Realms Personal & Business 3D Card • TYR Technical Consulting',
          url: shareUrl
        });
        return;
      } catch {}
    }
    try {
      await navigator.clipboard.writeText(shareUrl);
      alert(`Copied 3D Card link:\n${shareUrl}`);
    } catch {
      alert(shareUrl);
    }
  };

  function renderWorkflowPage(pageObj, container) {
    const workflowGrid = document.createElement('div');
    workflowGrid.className = 'workflow-grid';
    (pageObj?.workflow || []).forEach((wf) => {
      const wfCard = document.createElement('div');
      wfCard.className = 'workflow-card';
      wfCard.innerHTML = `
        <div class="workflow-step">${escapeHtml(wf.step)}</div>
        <h3>${escapeHtml(wf.title)}</h3>
        <p>${escapeHtml(wf.desc)}</p>
      `;
      workflowGrid.appendChild(wfCard);
    });
    container.appendChild(workflowGrid);

    const zeroBox = document.createElement('div');
    zeroBox.className = 'zero-custody-card';
    zeroBox.innerHTML = `
      <h3>ᛏ Zero-Custody Covenant Pledge</h3>
      <p>${escapeHtml(catalogData.entity.zeroCustodyPolicy)}</p>
      <div style="margin-top: 14px; display: flex; gap: 10px; flex-wrap: wrap;">
        <button onclick="openClientIntakeModal()" class="btn-primary">📝 Launch Client Intake</button>
        <button onclick="navigateToPage(20)" class="btn-stripe">💳 Open Stripe &amp; Receipts</button>
        <a href="tel:+16592578043" class="btn-primary btn-call">Direct Call (659) 257-8043</a>
      </div>
    `;
    container.appendChild(zeroBox);
  }

  function renderAllRealmsPage(container) {
    const modeBanner = document.createElement('div');
    modeBanner.className = `mode-banner-bar ${storefrontMode === 'business' ? 'mode-business' : ''}`;
    modeBanner.innerHTML = `
      <div>
        <strong style="color:#fff;">${storefrontMode === 'business' ? '🏢 Business / B2B Focus Active' : '👤 Personal & Solo Operator Focus Active'}</strong>
        <span style="margin-left:6px;font-size:11px;">${storefrontMode === 'business' ? 'Highlighting B2B governance, MSA suites & monthly retainers.' : 'Highlighting rapid one-time builds, DNS/email armor & personal expense audits.'}</span>
      </div>
      <button type="button" class="btn-secondary" style="font-size:11px;padding:5px 10px;" onclick="window.setStorefrontMode('${storefrontMode === 'business' ? 'personal' : 'business'}')">
        Switch to ${storefrontMode === 'business' ? '👤 Personal' : '🏢 Business'}
      </button>
    `;
    container.appendChild(modeBanner);

    const filterPanel = document.createElement('div');
    filterPanel.className = 'search-filter-panel';
    const categories = [
      ['ALL', 'All (31)'],
      ['REMOTE', '⚡ Remote (P7)'],
      ['B2B', '🏢 B2B & Retainers (P8-9)'],
      ['TECH', '🌐 Web & Shield'],
      ['LEGAL', '⚖️ Contracts & Admin'],
      ['FINANCE', '💰 Finance & Dispute'],
      ['CARE', '🛡️ Care Plans (P4-6)']
    ];

    filterPanel.innerHTML = `
      <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center;">
        <input type="search" id="all-realms-search-input" value="${escapeHtml(allRealmsSearch)}" placeholder="Search all 31 SKUs, realms, deliverables, or tags..." oninput="window.setAllRealmsSearch(this.value)" style="flex:1;min-width:220px;padding:9px 12px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;">
        <button class="compact-toggle-pill ${compactMode ? 'active' : ''}" onclick="toggleCompactMode()">${compactMode ? '▤ Compact: ON' : '▤ Compact: OFF'}</button>
      </div>
      <div class="filter-pills-row">
        ${categories
          .map(
            ([id, label]) =>
              `<button class="filter-pill ${allRealmsCategory === id ? 'active' : ''}" onclick="window.setAllRealmsCategory('${id}')">${escapeHtml(label)}</button>`
          )
          .join('')}
      </div>
    `;
    container.appendChild(filterPanel);

    const all = getAllServices();
    const q = allRealmsSearch.trim().toLowerCase();
    const filtered = all.filter((s) => {
      const matchText =
        !q ||
        s.title.toLowerCase().includes(q) ||
        s.sku.toLowerCase().includes(q) ||
        s.realm.toLowerCase().includes(q) ||
        (s.bullets || []).some((b) => b.toLowerCase().includes(q));
      const matchCat =
        allRealmsCategory === 'ALL' ||
        (allRealmsCategory === 'REMOTE' && s.pageNum === 7) ||
        (allRealmsCategory === 'B2B' && (s.pageNum === 8 || s.pageNum === 9)) ||
        (allRealmsCategory === 'TECH' && /SVC-(WEB|DOM|KIT|SEC|BCK)/.test(s.sku)) ||
        (allRealmsCategory === 'LEGAL' && /SVC-(CAR|OPS|SOP|DCK|B2B)/.test(s.sku)) ||
        (allRealmsCategory === 'FINANCE' && /SVC-(CMD|VLT|AUD|TAX|BUY)/.test(s.sku)) ||
        (allRealmsCategory === 'CARE' && (s.pageNum === 4 || s.pageNum === 5 || s.pageNum === 6));
      return matchText && matchCat;
    });

    if (filtered.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'card';
      empty.style.gridColumn = '1 / -1';
      empty.innerHTML = '<p style="color:var(--text-muted);margin:0;">No catalog SKUs matched your filter.</p>';
      container.appendChild(empty);
      return;
    }

    renderServiceCardsIntoGrid(filtered, container);
  }

  window.setAllRealmsSearch = function (val) {
    allRealmsSearch = val;
    renderPage(11);
    const inp = document.getElementById('all-realms-search-input');
    if (inp) {
      inp.focus();
      inp.setSelectionRange(val.length, val.length);
    }
  };

  window.setAllRealmsCategory = function (cat) {
    allRealmsCategory = cat;
    renderPage(11);
  };

  function renderCartPage(container) {
    const cart = window.TyrStripe.getCart();
    const activeRef = window.TyrGrowth ? window.TyrGrowth.getActiveReferralCode() : '';
    let rawOneTimeCents = 0;
    let monthlyCents = 0;
    cart.forEach((s) => {
      const c = window.TyrStripe.parseServicePriceToCents(s.price);
      if (window.TyrStripe.isMonthlyRecurringService(s)) monthlyCents += c;
      else rawOneTimeCents += c;
    });
    const refDiscountCents = activeRef && rawOneTimeCents > 0 ? Math.floor(rawOneTimeCents * 0.1) : 0;
    const oneTimeCents = Math.max(0, rawOneTimeCents - refDiscountCents);
    const totalToday = oneTimeCents + monthlyCents;

    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';

    if (cart.length === 0) {
      card.innerHTML = `
        <div style="text-align:center;padding:24px;">
          <h3 style="margin-bottom:8px;">Your Estimate Cart is Empty</h3>
          <p style="font-size:12px;color:var(--text-secondary);margin-bottom:14px;">Browse the Nine Realms catalog and tap "+ Quote" on any service SKU, or run the 60s Quick Audit.</p>
          <div style="display:flex;gap:8px;justify-content:center;flex-wrap:wrap;">
            <button class="btn-primary" onclick="navigateToPage(11)">🌐 Browse All 31 SKUs</button>
            <button class="btn-secondary" onclick="navigateToPage(22)">🚀 Run 60s Quick Audit</button>
          </div>
        </div>
      `;
    } else {
      card.innerHTML = `
        <div style="max-width:760px;margin:0 auto;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;flex-wrap:wrap;gap:8px;">
            <h3 style="margin:0;">Selected Estimate Items (${cart.length})</h3>
            <button class="btn-secondary" style="font-size:11px;padding:5px 10px;" onclick="window.TyrStripe.saveCart([]); renderPage(12);">Clear Quote Cart</button>
          </div>
          <div style="display:flex;flex-direction:column;gap:8px;margin-bottom:14px;">
            ${cart
              .map(
                (s) => `
              <div style="display:flex;justify-content:space-between;align-items:center;background:var(--bg-attr);border:1px solid var(--border-subtle);padding:10px;border-radius:var(--radius-sm);gap:8px;">
                <div>
                  <div style="font-size:12px;font-weight:700;color:#fff;">${escapeHtml(s.title)}</div>
                  <div style="font-size:10px;color:var(--text-gold);">${escapeHtml(s.sku)} • ${escapeHtml(s.price)}</div>
                </div>
                <button class="btn-secondary" style="font-size:10px;padding:4px 8px;color:#f87171;" onclick="window.toggleCartSku('${escapeHtml(s.sku)}')">Remove</button>
              </div>
            `
              )
              .join('')}
          </div>
          <div style="background:rgba(99,91,255,0.12);border:1px solid #635bff;border-radius:var(--radius-sm);padding:12px;margin-bottom:14px;">
            <div style="font-size:12px;color:#e2e8f0;">One-Time Deliverable Capital: <strong>${window.TyrStripe.formatCentsUsd(rawOneTimeCents)}</strong></div>
            ${activeRef ? `<div style="font-size:12px;color:#34d399;margin-top:2px;">10% Referral Credit (${escapeHtml(activeRef)}): <strong>-${window.TyrStripe.formatCentsUsd(refDiscountCents)}</strong></div>` : ''}
            <div style="font-size:12px;color:#e2e8f0;margin-top:2px;">Monthly Recurring Retainers: <strong>${window.TyrStripe.formatCentsUsd(monthlyCents)}/mo</strong></div>
            <div style="font-size:14px;color:#fff;font-weight:800;margin-top:6px;">Estimated Total Due Today: ${window.TyrStripe.formatCentsUsd(totalToday)}</div>
          </div>
          <div style="display:flex;gap:8px;flex-wrap:wrap;">
            <button class="btn-stripe" style="flex:1;justify-content:center;padding:11px;" onclick="window.openStripeForCart()">💳 Checkout with Stripe (Split / Full)</button>
            <button class="btn-primary" style="flex:1;padding:11px;" onclick="navigateToPage(13)">📝 Draft Formal Work Order</button>
          </div>
        </div>
      `;
    }
    container.appendChild(card);
  }

  window.openStripeForSku = function (sku) {
    const svc = getAllServices().find((s) => s.sku === sku);
    if (svc) window.openStripeCheckoutModal([svc]);
  };

  window.openStripeForCart = function () {
    const cart = window.TyrStripe.getCart();
    if (!cart.length) {
      alert('Add at least one SKU to your Quote Cart first.');
      return;
    }
    window.openStripeCheckoutModal(cart);
  };

  window.openStripeCheckoutModal = function (services) {
    stripeModalServices = services && services.length ? services : [];
    selectedSplitId = 'FULL_100';
    const modal = document.getElementById('stripe-checkout-modal');
    if (!modal) return;

    const sumEl = document.getElementById('stripe-modal-items-summary');
    if (sumEl) {
      sumEl.innerHTML = stripeModalServices
        .map((s) => `<div><strong>${escapeHtml(s.sku)}</strong> — ${escapeHtml(s.title)} <span style="color:var(--text-gold);font-family:var(--font-mono);">${escapeHtml(s.price)}</span></div>`)
        .join('');
    }

    const splitBtnsEl = document.getElementById('stripe-split-buttons');
    if (splitBtnsEl) {
      splitBtnsEl.innerHTML = window.TyrStripe.SPLIT_OPTIONS.map(
        (opt) => `
        <button type="button" class="filter-pill ${opt.id === selectedSplitId ? 'active' : ''}" onclick="window.selectStripeSplit('${opt.id}')">
          ${escapeHtml(opt.label)}
        </button>`
      ).join('');
    }

    const customWrap = document.getElementById('stripe-custom-split-wrap');
    if (customWrap) customWrap.style.display = 'none';

    if (chainedWorkOrder) {
      const nEl = document.getElementById('stripe-modal-name');
      const eEl = document.getElementById('stripe-modal-email');
      const bEl = document.getElementById('stripe-modal-business');
      if (nEl && !nEl.value) nEl.value = chainedWorkOrder.clientName || '';
      if (eEl && !eEl.value) eEl.value = chainedWorkOrder.email || '';
      if (bEl && !bEl.value) bEl.value = chainedWorkOrder.businessName || '';
    }

    window.updateStripeSplitPreview();
    modal.style.display = 'flex';
  };

  window.closeStripeCheckoutModal = function () {
    const modal = document.getElementById('stripe-checkout-modal');
    if (modal) modal.style.display = 'none';
  };

  window.selectStripeSplit = function (splitId) {
    selectedSplitId = splitId;
    document.querySelectorAll('#stripe-split-buttons .filter-pill').forEach((btn, idx) => {
      const opt = window.TyrStripe.SPLIT_OPTIONS[idx];
      btn.classList.toggle('active', opt && opt.id === splitId);
    });
    const customWrap = document.getElementById('stripe-custom-split-wrap');
    if (customWrap) customWrap.style.display = splitId === 'CUSTOM_SPLIT' ? 'block' : 'none';
    window.updateStripeSplitPreview();
  };

  function getActiveSplitPercent() {
    if (selectedSplitId === 'CUSTOM_SPLIT') {
      return parseInt(document.getElementById('stripe-custom-split-slider')?.value || '25', 10);
    }
    const found = window.TyrStripe.SPLIT_OPTIONS.find((o) => o.id === selectedSplitId);
    return found ? found.defaultPercent : 100;
  }

  window.updateStripeSplitPreview = function () {
    const pct = getActiveSplitPercent();
    const labelEl = document.getElementById('stripe-custom-split-val');
    if (labelEl) labelEl.textContent = `${pct}%`;

    const activeRef = window.TyrGrowth ? window.TyrGrowth.getActiveReferralCode() : '';
    let rawOneTimeCents = 0;
    let monthlyCents = 0;
    stripeModalServices.forEach((s) => {
      const c = window.TyrStripe.parseServicePriceToCents(s.price);
      if (window.TyrStripe.isMonthlyRecurringService(s)) monthlyCents += c;
      else rawOneTimeCents += window.TyrStripe.applySplitToCents(c, pct);
    });
    const refDiscount = activeRef && rawOneTimeCents > 0 ? Math.floor(rawOneTimeCents * 0.1) : 0;
    const total = Math.max(0, rawOneTimeCents - refDiscount) + monthlyCents;
    const banner = document.getElementById('stripe-split-due-banner');
    if (banner) {
      banner.textContent = activeRef && refDiscount > 0
        ? `Due Today (${pct}% Split - 10% Ref ${activeRef}): ${window.TyrStripe.formatCentsUsd(total)}`
        : `Due Today (${pct}% One-Time Split + Recurring): ${window.TyrStripe.formatCentsUsd(total)}`;
    }
  };

  window.submitStripeModalCheckout = async function (e) {
    e.preventDefault();
    const clientName = document.getElementById('stripe-modal-name')?.value.trim() || '';
    const clientEmail = document.getElementById('stripe-modal-email')?.value.trim() || '';
    const businessName = document.getElementById('stripe-modal-business')?.value.trim() || '';
    const splitPercent = getActiveSplitPercent();

    const res = await window.TyrStripe.processCheckout({
      services: stripeModalServices,
      clientName,
      businessName,
      clientEmail,
      splitPercent
    });

    window.closeStripeCheckoutModal();

    if (res.hostedCheckoutUrl) {
      window.location.href = res.hostedCheckoutUrl;
      return;
    }

    alert(`Receipt ${res.record.receiptNumber} minted with SHA-384 verification! Opening Receipts Hub.`);
    stripeSubTab = 'RECEIPTS';
    window.navigateToPage(20);
  };

  function renderStripeHubPage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';

    const receipts = window.TyrStripe.getReceipts();
    const priceMap = window.TyrStripe.getCustomSkuPriceMap();
    const backendEndpoint = window.TyrStripe.getBackendEndpoint();
    const allServices = getAllServices();

    let bodyHtml = '';
    if (stripeSubTab === 'RECEIPTS') {
      const rowsHtml =
        receipts.length === 0
          ? `<div style="text-align:center;padding:24px;color:var(--text-muted);font-size:12px;">No settled receipts yet. Select any catalog SKU and click <strong>💳 Stripe Pay</strong>.</div>`
          : receipts
              .map(
                (r) => `
              <div style="background:var(--bg-attr);border:1px solid #10b981;border-radius:var(--radius-sm);padding:12px;margin-bottom:10px;">
                <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:6px;">
                  <span style="font-family:var(--font-mono);font-weight:800;color:#34d399;font-size:12px;">✓ ${escapeHtml(r.receiptNumber)}</span>
                  <span style="font-family:var(--font-mono);font-weight:800;color:#fff;font-size:13px;">${window.TyrStripe.formatCentsUsd(r.totalChargedTodayCents)}</span>
                </div>
                <div style="font-size:11px;color:var(--text-secondary);margin-top:4px;">${escapeHtml(r.clientName)} (${escapeHtml(r.clientEmail)}) • ${escapeHtml(r.timestampIso)}</div>
                <div style="font-size:11px;color:var(--text-gold);margin-top:2px;">SKUs: ${escapeHtml((r.skus || []).join(', '))} • Session: ${escapeHtml(r.checkoutSessionId)}</div>
                <div style="font-family:var(--font-mono);font-size:9px;color:#38bdf8;margin-top:6px;word-break:break-all;">SHA-384: ${escapeHtml(r.sha384IntegrityHash)}</div>
              </div>`
              )
              .join('');

      bodyHtml = `
        <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:14px;">
          <button class="btn-primary" style="background:#059669;" onclick="window.exportStripeLedgerMd()">📥 Download Schedule C Ledger (.md)</button>
          <button class="btn-stripe" onclick="window.exportStripeLedgerCsv()">📊 Download Schedule C CSV (.csv)</button>
          <button class="btn-secondary" style="color:#f87171;" onclick="if(confirm('Purge all local receipts?')){ window.TyrStripe.purgeReceipts(); renderPage(20); }">🗑️ 0-Day Purge Vault</button>
        </div>
        ${rowsHtml}
      `;
    } else if (stripeSubTab === 'MAPPER') {
      bodyHtml = `
        <div style="background:rgba(99,91,255,0.12);border:1px solid #635bff;border-radius:var(--radius-sm);padding:12px;margin-bottom:14px;">
          <div style="font-size:12px;font-weight:700;color:#fff;">Checkout Studio Plan: <code>${window.TyrStripe.CHECKOUT_PLAN_ID}</code></div>
          <div style="font-size:11px;color:#cbd5e1;margin-top:4px;">Account: <code>${window.TyrStripe.STRIPE_ACCOUNT_ID}</code> • Authoritative <code>ui_mode=hosted_page</code>, <code>automatic_tax</code>, <code>tax_id_collection</code>, <code>consent_collection=required</code></div>
          <div style="margin-top:10px;">
            <label style="font-size:10px;color:var(--text-gold);font-weight:700;display:block;margin-bottom:4px;">OPTIONAL BACKEND CHECKOUT ENDPOINT URL (Add once server + webhook secret are live)</label>
            <div style="display:flex;gap:8px;flex-wrap:wrap;">
              <input type="url" id="stripe-backend-url-input" value="${escapeHtml(backendEndpoint)}" placeholder="https://api.tyrprotocol.pro/checkout/sessions" style="flex:1;min-width:220px;padding:8px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:11px;font-family:monospace;">
              <button class="btn-primary" onclick="window.saveStripeBackendUrl()">Save Endpoint</button>
            </div>
          </div>
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
          <strong style="font-size:12px;color:#fff;">In-App Stripe Price ID (<code>price_...</code>) Mapper per SKU</strong>
          <button class="btn-secondary" style="font-size:10px;padding:4px 8px;" onclick="window.TyrStripe.clearAllCustomSkuPriceIds(); renderPage(20);">Clear All</button>
        </div>
        <div style="display:flex;flex-direction:column;gap:8px;max-height:380px;overflow-y:auto;">
          ${allServices
            .map(
              (s) => `
            <div style="display:flex;align-items:center;gap:8px;background:var(--bg-attr);border:1px solid var(--border-subtle);padding:8px;border-radius:var(--radius-sm);flex-wrap:wrap;">
              <div style="flex:1;min-width:180px;">
                <div style="font-size:11px;font-weight:700;color:#fff;">${escapeHtml(s.sku)} — ${escapeHtml(s.title)}</div>
                <div style="font-size:10px;color:var(--text-gold);">${escapeHtml(s.price)}</div>
              </div>
              <input type="text" id="price-map-${escapeHtml(s.sku)}" value="${escapeHtml(priceMap[s.sku] || '')}" placeholder="price_1..." style="width:180px;padding:6px 8px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:11px;font-family:monospace;">
              <button class="btn-secondary" style="font-size:10px;padding:5px 9px;" onclick="window.saveSkuPriceMapping('${escapeHtml(s.sku)}')">Save</button>
            </div>`
            )
            .join('')}
        </div>
      `;
    } else {
      bodyHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <h4 style="margin:0 0 8px 0;color:#fff;font-size:13px;">🔗 URL Return &amp; Deep-Link Session Reconciliation</h4>
          <p style="font-size:11px;color:var(--text-secondary);line-height:1.5;margin-bottom:10px;">
            When Stripe Hosted Checkout redirects back to <code>https://tyrprotocol.pro/?status=success&amp;session_id={CHECKOUT_SESSION_ID}</code>, the storefront reconciles the session into your SHA-384 Receipt Vault automatically.
          </p>
          <button class="btn-stripe" onclick="window.simulateStripeReturnTest()">Test Return Reconciliation (?session_id=cs_test_tyr2026)</button>
        </div>
      `;
    }

    card.innerHTML = `
      <div style="max-width:820px;margin:0 auto;">
        <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:14px;">
          <button class="filter-pill ${stripeSubTab === 'RECEIPTS' ? 'active' : ''}" onclick="window.setStripeSubTab('RECEIPTS')">🧾 Receipts &amp; Schedule C (${receipts.length})</button>
          <button class="filter-pill ${stripeSubTab === 'MAPPER' ? 'active' : ''}" onclick="window.setStripeSubTab('MAPPER')">🏷️ Price IDs &amp; Checkout Plan</button>
          <button class="filter-pill ${stripeSubTab === 'RECONCILE' ? 'active' : ''}" onclick="window.setStripeSubTab('RECONCILE')">🔗 Return Reconciliation</button>
        </div>
        ${bodyHtml}
      </div>
    `;
    container.appendChild(card);
  }

  window.setStripeSubTab = function (tab) {
    stripeSubTab = tab;
    renderPage(20);
  };

  window.saveStripeBackendUrl = function () {
    const val = document.getElementById('stripe-backend-url-input')?.value || '';
    window.TyrStripe.setBackendEndpoint(val);
    alert(val.trim() ? 'Backend Checkout Session endpoint saved.' : 'Cleared backend URL (client SAQ-A mode active).');
    renderPage(20);
  };

  window.saveSkuPriceMapping = function (sku) {
    const val = document.getElementById(`price-map-${sku}`)?.value || '';
    window.TyrStripe.setCustomSkuPriceId(sku, val);
    alert(`Saved custom Stripe Price ID mapping for ${sku}.`);
    renderPage(20);
  };

  window.exportStripeLedgerMd = function () {
    const md = window.TyrStripe.exportScheduleCMarkdown();
    window.TyrStripe.downloadTextFile('transaction_audit_ledger_2026.md', md, 'text/markdown');
  };

  window.exportStripeLedgerCsv = function () {
    const csv = window.TyrStripe.exportScheduleCCsv();
    window.TyrStripe.downloadTextFile('transaction_audit_ledger_2026.csv', csv, 'text/csv');
  };

  window.simulateStripeReturnTest = function () {
    const url = new URL(window.location.href);
    url.searchParams.set('status', 'success');
    url.searchParams.set('session_id', 'cs_test_tyr2026');
    window.history.replaceState({}, document.title, url.toString());
    window.TyrStripe.reconcileQueryReturn().then((res) => {
      if (res) {
        alert(res.message);
        stripeSubTab = 'RECEIPTS';
        renderPage(20);
      }
    });
  };

  function renderForensicsHubPage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';

    let tabHtml = '';
    if (forensicsSubTab === 'HASHER') {
      tabHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <h4 style="margin:0 0 6px 0;color:#fff;">1. Local WebCrypto SHA-384 File &amp; Payload Hasher</h4>
          <p style="font-size:11px;color:var(--text-secondary);margin-bottom:10px;">Zero uploads — files are hashed locally in browser RAM via <code>crypto.subtle.digest('SHA-384')</code>.</p>
          <div style="margin-bottom:12px;">
            <label style="font-size:10px;color:var(--text-gold);font-weight:700;display:block;margin-bottom:4px;">SELECT LOCAL FILE TO HASH</label>
            <input type="file" id="forensics-file-input" onchange="window.runWebFileHash(this.files[0])" style="font-size:12px;color:#cbd5e1;">
          </div>
          <div style="margin-bottom:10px;">
            <label style="font-size:10px;color:var(--text-gold);font-weight:700;display:block;margin-bottom:4px;">OR HASH TEXT PAYLOAD</label>
            <div style="display:flex;gap:8px;flex-wrap:wrap;">
              <input type="text" id="forensics-text-payload" value="TYR-FORENSIC-PRE-REMEDIATION-BASELINE-2026" style="flex:1;min-width:220px;padding:8px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:11px;font-family:monospace;">
              <button class="btn-primary" onclick="window.runWebTextHash()">Compute SHA-384</button>
            </div>
          </div>
          <div id="forensics-hash-output" style="display:none;background:#020617;border:1px solid #38bdf8;border-radius:var(--radius-sm);padding:10px;font-family:var(--font-mono);font-size:10px;color:#38bdf8;word-break:break-all;"></div>
        </div>
      `;
    } else if (forensicsSubTab === 'EXIF') {
      tabHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <h4 style="margin:0 0 6px 0;color:#fff;">2. One-Tap EXIF &amp; GPS Metadata Scrubber</h4>
          <p style="font-size:11px;color:var(--text-secondary);margin-bottom:10px;">Re-encodes images cleanly on an offscreen HTML5 Canvas, purging GPS coordinates &amp; camera serials and normalizing region to coarse <strong>Northwest Alabama</strong>.</p>
          <input type="file" accept="image/*" id="forensics-exif-input" onchange="window.runWebExifScrub(this.files[0])" style="font-size:12px;color:#cbd5e1;margin-bottom:10px;">
          <div id="forensics-exif-output" style="display:none;background:#022c22;border:1px solid #10b981;border-radius:var(--radius-sm);padding:10px;font-size:11px;color:#a7f3d0;"></div>
        </div>
      `;
    } else if (forensicsSubTab === 'RCA') {
      tabHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <h4 style="margin:0 0 6px 0;color:#fff;">3. Offline Diagnostic Checklist &amp; Telemetry Log Builder</h4>
          <p style="font-size:11px;color:var(--text-secondary);margin-bottom:10px;">Mobile-formatted (&le; 36 chars/line) CLI checks with mandatory rollback pairs &amp; non-lockout VeraCrypt / 7-Zip AES-256 baseline (0 BitLocker).</p>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:10px;">
            <input type="text" id="rca-host-input" value="WORKSTATION-AL-01" placeholder="Target Host Alias" style="padding:8px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:12px;">
            <input type="text" id="rca-notes-input" placeholder="RCA Notes (Optional)" style="padding:8px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:12px;">
          </div>
          <div style="display:flex;flex-direction:column;gap:6px;margin-bottom:12px;">
            ${window.TyrForensics.DIAGNOSTIC_CHECKS.map(
              (chk) => `
              <label style="display:flex;align-items:flex-start;gap:8px;background:#07070b;padding:8px;border-radius:var(--radius-sm);border:1px solid var(--border-subtle);font-size:11px;cursor:pointer;">
                <input type="checkbox" class="rca-chk-box" value="${escapeHtml(chk.id)}" checked style="margin-top:2px;">
                <div>
                  <strong style="color:#fff;">[${escapeHtml(chk.id)}] ${escapeHtml(chk.title)}</strong>
                  <pre style="margin:4px 0 0 0;font-size:10px;color:#38bdf8;white-space:pre-wrap;">${escapeHtml(chk.cliSnippet)}</pre>
                </div>
              </label>`
            ).join('')}
          </div>
          <button class="btn-primary" style="background:#059669;" onclick="window.downloadWebTelemetryLog()">📥 Build &amp; Download Telemetry Log (.md)</button>
        </div>
      `;
    } else {
      tabHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <h4 style="margin:0 0 8px 0;color:#fff;">4. Automated Pre-Service Intake → E-Sign SOW → Stripe Pipeline</h4>
          <p style="font-size:11px;color:var(--text-secondary);line-height:1.5;margin-bottom:12px;">Chain client engagement from Work Order generation straight into WebCrypto SHA-384 contract execution and tokenized Stripe deposit or full settlement.</p>
          <div style="display:flex;gap:8px;flex-wrap:wrap;">
            <button class="btn-primary" onclick="navigateToPage(13)">Step 1: Open Intake (P13)</button>
            <button class="btn-secondary" onclick="navigateToPage(17)">Step 2: Open E-Sign SOW (P17)</button>
            <button class="btn-stripe" onclick="navigateToPage(20)">Step 3: Open Stripe Hub (P20)</button>
          </div>
        </div>
      `;
    }

    card.innerHTML = `
      <div style="max-width:820px;margin:0 auto;">
        <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:14px;">
          <button class="filter-pill ${forensicsSubTab === 'HASHER' ? 'active' : ''}" onclick="window.setForensicsSubTab('HASHER')">🔐 SHA-384 Hasher</button>
          <button class="filter-pill ${forensicsSubTab === 'EXIF' ? 'active' : ''}" onclick="window.setForensicsSubTab('EXIF')">🖼️ EXIF Scrubber</button>
          <button class="filter-pill ${forensicsSubTab === 'RCA' ? 'active' : ''}" onclick="window.setForensicsSubTab('RCA')">🩺 RCA Telemetry</button>
          <button class="filter-pill ${forensicsSubTab === 'PIPELINE' ? 'active' : ''}" onclick="window.setForensicsSubTab('PIPELINE')">⚡ Chained Pipeline</button>
        </div>
        ${tabHtml}
      </div>
    `;
    container.appendChild(card);
  }

  window.setForensicsSubTab = function (tab) {
    forensicsSubTab = tab;
    renderPage(21);
  };

  window.runWebFileHash = async function (file) {
    if (!file) return;
    const out = document.getElementById('forensics-hash-output');
    try {
      const res = await window.TyrForensics.hashFileSha384(file);
      if (out) {
        out.style.display = 'block';
        out.innerHTML = `File: ${escapeHtml(res.fileName)} (${res.byteSize} bytes)<br>KDF Baseline: ${escapeHtml(res.kdfSpec)}<br>SHA-384: ${escapeHtml(res.sha384Digest)}`;
      }
    } catch (err) {
      alert(err.message || 'Hashing failed');
    }
  };

  window.runWebTextHash = async function () {
    const val = document.getElementById('forensics-text-payload')?.value || '';
    const out = document.getElementById('forensics-hash-output');
    try {
      const digest = await window.TyrStripe.sha384Hex(val);
      if (out) {
        out.style.display = 'block';
        out.innerHTML = `Payload (${val.length} chars)<br>SHA-384: ${escapeHtml(digest)}`;
      }
    } catch (err) {
      alert(err.message || 'Hashing failed');
    }
  };

  window.runWebExifScrub = async function (file) {
    if (!file) return;
    const out = document.getElementById('forensics-exif-output');
    try {
      const res = await window.TyrForensics.scrubImageExif(file);
      if (out) {
        out.style.display = 'block';
        out.innerHTML = `
          <div><strong>✓ Metadata Stripped:</strong> ${escapeHtml(res.sourceLabel)} (${escapeHtml(res.dimensions)})</div>
          <div>Region Normalized: <strong>${escapeHtml(res.normalizedLocation)}</strong></div>
          <div style="font-family:var(--font-mono);font-size:9px;margin-top:4px;word-break:break-all;">Original SHA-384: ${escapeHtml(res.originalSha384)}</div>
          <div style="font-family:var(--font-mono);font-size:9px;margin-top:2px;word-break:break-all;">Clean SHA-384: ${escapeHtml(res.scrubbedSha384)}</div>
          <div style="margin-top:8px;">
            <a href="${escapeHtml(res.cleanBlobUrl)}" download="${escapeHtml(res.sourceLabel)}" class="btn-primary" style="display:inline-flex;padding:6px 12px;font-size:11px;text-decoration:none;">⬇️ Download Scrubbed Clean PNG</a>
          </div>
        `;
      }
    } catch (err) {
      alert(err.message || 'EXIF scrub failed');
    }
  };

  window.downloadWebTelemetryLog = async function () {
    const host = document.getElementById('rca-host-input')?.value || 'LOCAL-WORKSTATION-01';
    const notes = document.getElementById('rca-notes-input')?.value || '';
    const checked = [];
    document.querySelectorAll('.rca-chk-box:checked').forEach((cb) => checked.push(cb.value));
    const md = await window.TyrForensics.buildTelemetryLogMarkdown(host, notes, checked);
    window.TyrStripe.downloadTextFile('field_telemetry_rca_log.md', md, 'text/markdown');
  };

  window.shareServiceSku = async function (sku) {
    const svc = getAllServices().find((s) => s.sku === sku);
    if (!svc || !window.TyrGrowth) return;
    const url = window.TyrGrowth.buildShareUrl(svc.sku, true);
    const pitch = window.TyrGrowth.buildPitchText('SMS', svc, true);
    window.TyrGrowth.recordGrowthEvent('SHARE', `Shared SKU deep-link ${svc.sku}`);

    if (navigator.share) {
      try {
        await navigator.share({
          title: `TYR Consulting — ${svc.title} (${svc.price})`,
          text: pitch,
          url
        });
        return;
      } catch {}
    }
    try {
      await navigator.clipboard.writeText(pitch);
      alert(`Copied deep-link pitch for ${svc.sku}:\n${url}`);
    } catch {
      pitchSku = svc.sku;
      growthSubTab = 'PITCH';
      window.navigateToPage(22);
    }
  };

  function renderGrowthHubPage(container) {
    const G = window.TyrGrowth;
    if (!G) return;
    const allServices = getAllServices();
    const ownRef = G.getOwnReferralCode();
    const activeRef = G.getActiveReferralCode();
    const ledger = G.getGrowthLedger();

    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';

    let bodyHtml = '';
    if (growthSubTab === 'QUIZ') {
      const result = G.evaluateQuiz(quizAnswers);
      const matchedServices = result.recommendedSkus
        .map((sku) => allServices.find((s) => s.sku === sku))
        .filter(Boolean);
      const scoreColor = result.score >= 80 ? '#34d399' : result.score >= 50 ? '#fbbf24' : '#f87171';

      bodyHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;margin-bottom:14px;">
          <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px;margin-bottom:10px;">
            <div>
              <h4 style="margin:0;color:#fff;font-size:14px;">⚡ 60-Second Security &amp; Overhead Self-Audit</h4>
              <p style="font-size:11px;color:var(--text-secondary);margin:2px 0 0 0;">Zero cost, zero email gate — surfaces leaks and routes directly to matched deliverables.</p>
            </div>
            <div style="text-align:right;">
              <div style="font-family:var(--font-mono);font-size:20px;font-weight:900;color:${scoreColor};">${result.score}/100</div>
              <div style="font-size:10px;color:var(--text-muted);">Est. Annual Leak: $${result.estAnnualLeakUsd}/yr</div>
            </div>
          </div>

          <div style="display:flex;flex-direction:column;gap:12px;">
            ${G.QUIZ_QUESTIONS.map(
              (q) => `
              <div style="background:#07070b;border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:10px;">
                <div style="font-size:12px;font-weight:700;color:#fff;margin-bottom:8px;">${escapeHtml(q.prompt)}</div>
                <div style="display:flex;flex-direction:column;gap:6px;">
                  ${q.options
                    .map((opt, idx) => {
                      const active = Number(quizAnswers[q.id] ?? 0) === idx;
                      return `<button type="button" class="filter-pill ${active ? 'active' : ''}" style="text-align:left;justify-content:flex-start;padding:8px 10px;border-radius:var(--radius-sm);" onclick="window.setQuizAnswer('${escapeHtml(q.id)}', ${idx})">${active ? '✓ ' : ''}${escapeHtml(opt.label)}</button>`;
                    })
                    .join('')}
                </div>
              </div>
            `
            ).join('')}
          </div>
        </div>

        <div style="background:rgba(16,185,129,0.08);border:1px solid #059669;border-radius:var(--radius-sm);padding:14px;">
          <div style="font-size:12px;font-weight:800;color:#34d399;margin-bottom:8px;">RECOMMENDED ACTION PLAN (${matchedServices.length} SKUs)</div>
          <div style="display:flex;flex-direction:column;gap:6px;margin-bottom:12px;">
            ${matchedServices
              .map(
                (s) => `
              <div style="display:flex;justify-content:space-between;align-items:center;background:var(--bg-attr);border:1px solid var(--border-subtle);padding:8px 10px;border-radius:var(--radius-sm);gap:8px;flex-wrap:wrap;">
                <div>
                  <strong style="color:#fff;font-size:12px;">${escapeHtml(s.sku)} — ${escapeHtml(s.title)}</strong>
                  <div style="font-size:10px;color:var(--text-secondary);">${escapeHtml(s.badge)}</div>
                </div>
                <span style="font-family:var(--font-mono);color:var(--text-gold);font-weight:800;font-size:12px;">${escapeHtml(s.price)}</span>
              </div>`
              )
              .join('')}
          </div>
          <div style="display:flex;gap:8px;flex-wrap:wrap;">
            <button class="btn-stripe" onclick="window.applyQuizMatchesToStripe()">💳 Launch Split Checkout for Matches</button>
            <button class="btn-primary" onclick="window.applyQuizMatchesToCart()">🧮 Add Matches to Quote Cart</button>
            <button class="btn-secondary" onclick="window.applyQuizMatchesToIntake()">📝 Pre-Fill Intake Work Order</button>
          </div>
        </div>
      `;
    } else if (growthSubTab === 'PITCH') {
      const selectedSvc = allServices.find((s) => s.sku === pitchSku) || allServices[0];
      const pitchText = G.buildPitchText(pitchChannel, selectedSvc, pitchIncludeRef);
      const deepLink = G.buildShareUrl(selectedSvc?.sku || '', pitchIncludeRef);

      bodyHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <h4 style="margin:0 0 8px 0;color:#fff;">📣 One-Tap Organic Pitch &amp; Deep-Link Generator</h4>
          <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:10px;">
            ${['NEXTDOOR', 'FACEBOOK', 'SMS', 'EMAIL']
              .map(
                (ch) =>
                  `<button class="filter-pill ${pitchChannel === ch ? 'active' : ''}" onclick="window.updatePitchChannel('${ch}')">${ch}</button>`
              )
              .join('')}
          </div>
          <div style="display:grid;grid-template-columns:1fr auto;gap:8px;align-items:center;margin-bottom:10px;">
            <select id="growth-pitch-sku" onchange="window.updatePitchSku(this.value)" style="width:100%;padding:8px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:12px;">
              ${allServices
                .map(
                  (s) =>
                    `<option value="${escapeHtml(s.sku)}" ${s.sku === pitchSku ? 'selected' : ''}>${escapeHtml(s.sku)} — ${escapeHtml(s.title)} (${escapeHtml(s.price)})</option>`
                )
                .join('')}
            </select>
            <label style="font-size:11px;color:var(--text-gold);display:flex;align-items:center;gap:5px;white-space:nowrap;cursor:pointer;">
              <input type="checkbox" ${pitchIncludeRef ? 'checked' : ''} onchange="window.togglePitchRef(this.checked)"> +10% Ref (${escapeHtml(ownRef)})
            </label>
          </div>
          <div style="font-family:var(--font-mono);font-size:10px;color:#38bdf8;background:#020617;padding:7px 10px;border-radius:var(--radius-sm);margin-bottom:8px;word-break:break-all;">
            Deep Link: ${escapeHtml(deepLink)}
          </div>
          <textarea id="growth-pitch-textarea" rows="6" style="width:100%;padding:10px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:12px;line-height:1.45;margin-bottom:10px;">${escapeHtml(pitchText)}</textarea>
          <div style="display:flex;gap:8px;flex-wrap:wrap;">
            <button class="btn-primary" onclick="window.copyPitchText()">📋 Copy Pitch + Deep Link</button>
            <button class="btn-secondary" onclick="window.nativeSharePitch()">↗ Native OS Share</button>
            <button class="btn-secondary" onclick="window.TyrGrowth.downloadVCard()">⬇ Download .vcf Contact</button>
            <a href="./assets/3d_card.html" target="_blank" rel="noopener noreferrer" class="btn-secondary">🪪 Open 3D Card</a>
          </div>
        </div>
      `;
    } else if (growthSubTab === 'FLYER') {
      const selectedSvc = allServices.find((s) => s.sku === flyerSku) || allServices[0];
      const targetUrl =
        flyerTargetType === 'VCARD'
          ? G.MECARD_QR_STRING
          : flyerTargetType === 'SKU'
          ? G.buildShareUrl(selectedSvc?.sku || 'SVC-WEB-01', flyerIncludeRef)
          : G.buildShareUrl('', flyerIncludeRef);
      const qrSvg = G.renderQrSvg(targetUrl, '#090a0f', '#ffffff', 160);

      bodyHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <h4 style="margin:0 0 8px 0;color:#fff;">🖨️ Printable QR Flyer &amp; Leave-Behind Pocket Card Builder</h4>
          <p style="font-size:11px;color:var(--text-secondary);margin-bottom:10px;">Zero-API offline vector QR generator for truck magnets, counter signs, and 3.5×2 client leave-behind cards.</p>
          <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:10px;">
            <button class="filter-pill ${flyerMode === 'FLYER' ? 'active' : ''}" onclick="window.setFlyerMode('FLYER')">📄 Counter / Bulletin Flyer (8.5×11)</button>
            <button class="filter-pill ${flyerMode === 'CARD' ? 'active' : ''}" onclick="window.setFlyerMode('CARD')">🪪 Pocket Leave-Behind Card (3.5×2)</button>
          </div>
          <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:10px;">
            <button class="filter-pill ${flyerTargetType === 'STOREFRONT' ? 'active' : ''}" onclick="window.setFlyerTargetType('STOREFRONT')">🌐 Storefront + Ref QR</button>
            <button class="filter-pill ${flyerTargetType === 'SKU' ? 'active' : ''}" onclick="window.setFlyerTargetType('SKU')">🏷️ Specific SKU QR</button>
            <button class="filter-pill ${flyerTargetType === 'VCARD' ? 'active' : ''}" onclick="window.setFlyerTargetType('VCARD')">👤 Offline Contact (MeCard) QR</button>
          </div>
          ${
            flyerTargetType === 'SKU'
              ? `<select onchange="window.setFlyerSku(this.value)" style="width:100%;padding:8px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-size:12px;margin-bottom:10px;">
                  ${allServices
                    .map(
                      (s) =>
                        `<option value="${escapeHtml(s.sku)}" ${s.sku === flyerSku ? 'selected' : ''}>${escapeHtml(s.sku)} — ${escapeHtml(s.title)} (${escapeHtml(s.price)})</option>`
                    )
                    .join('')}
                </select>`
              : ''
          }
          <label style="font-size:11px;color:var(--text-gold);display:inline-flex;align-items:center;gap:6px;margin-bottom:12px;cursor:pointer;">
            <input type="checkbox" ${flyerIncludeRef ? 'checked' : ''} onchange="window.toggleFlyerRef(this.checked)"> Stamp Referral Code ${escapeHtml(ownRef)} (10% Welcome Credit)
          </label>

          <div style="display:grid;grid-template-columns:1fr auto;gap:14px;align-items:center;background:#07070b;border:1px solid var(--border-highlight);border-radius:var(--radius-md);padding:14px;margin-bottom:12px;">
            <div>
              <div style="font-size:10px;color:var(--text-gold);font-weight:800;">PREVIEW • ${flyerMode === 'CARD' ? '3.5×2 LEAVE-BEHIND' : '8.5×11 FLYER'}</div>
              <div style="font-size:14px;font-weight:800;color:#fff;margin-top:4px;">${flyerTargetType === 'SKU' && selectedSvc ? escapeHtml(selectedSvc.title + ' — ' + selectedSvc.price) : 'TYR Technical Consulting (NW Alabama & Remote)'}</div>
              <div style="font-family:var(--font-mono);font-size:10px;color:#38bdf8;margin-top:6px;word-break:break-all;">${escapeHtml(targetUrl)}</div>
              <div style="font-size:11px;color:#cbd5e1;margin-top:6px;">📞 (659) 257-8043 • ✉️ tyr@tyrprotocol.pro</div>
            </div>
            <div style="background:#fff;padding:8px;border-radius:8px;text-align:center;">
              ${qrSvg}
            </div>
          </div>

          <div style="display:flex;gap:8px;flex-wrap:wrap;">
            <button class="btn-primary" onclick="window.printQrCollateral()">🖨️ Open Print / PDF Dialog</button>
            <button class="btn-secondary" onclick="window.downloadQrCollateralHtml()">📥 Download Printable HTML</button>
          </div>
        </div>
      `;
    } else if (growthSubTab === 'PROOF') {
      bodyHtml = `
        <div style="background:rgba(251,191,36,0.08);border:1px solid #d97706;border-radius:var(--radius-sm);padding:14px;margin-bottom:14px;">
          <h4 style="margin:0 0 8px 0;color:#fbbf24;">🤝 Client Referral Reward Code &amp; Zero-Cost Attribution</h4>
          <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(130px,1fr));gap:8px;margin-bottom:10px;">
            <div style="background:#07070b;padding:8px;border-radius:var(--radius-sm);border:1px solid var(--border-subtle);">
              <div style="font-size:9px;color:var(--text-muted);">YOUR REF CODE</div>
              <div style="font-family:var(--font-mono);font-size:13px;font-weight:800;color:#fbbf24;">${escapeHtml(ownRef)}</div>
            </div>
            <div style="background:#07070b;padding:8px;border-radius:var(--radius-sm);border:1px solid var(--border-subtle);">
              <div style="font-size:9px;color:var(--text-muted);">OUTBOUND SHARES</div>
              <div style="font-family:var(--font-mono);font-size:13px;font-weight:800;color:#fff;">${ledger.sharesDispatched}</div>
            </div>
            <div style="background:#07070b;padding:8px;border-radius:var(--radius-sm);border:1px solid var(--border-subtle);">
              <div style="font-size:9px;color:var(--text-muted);">REF CREDITS TRACKED</div>
              <div style="font-family:var(--font-mono);font-size:13px;font-weight:800;color:#34d399;">$${Number(ledger.creditsIssuedUsd || 0).toFixed(2)}</div>
            </div>
          </div>
          <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:8px;">
            <input type="text" id="own-ref-code-input" value="${escapeHtml(ownRef)}" placeholder="TYR-843-REF" style="flex:1;min-width:150px;padding:7px 9px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#fff;font-family:monospace;font-size:12px;">
            <button class="btn-secondary" onclick="window.saveOwnRefCode()">Update Share Code</button>
          </div>
          <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center;">
            <input type="text" id="active-ref-code-input" value="${escapeHtml(activeRef)}" placeholder="Apply a neighbor's referral code (10% off)" style="flex:1;min-width:190px;padding:7px 9px;border-radius:var(--radius-sm);background:#07070b;border:1px solid var(--border-subtle);color:#34d399;font-family:monospace;font-size:12px;">
            <button class="btn-primary" style="background:#059669;" onclick="window.applyManualActiveRef()">Apply 10% Credit</button>
            ${activeRef ? `<button class="btn-secondary" onclick="window.TyrGrowth.setActiveReferralCode(''); renderPage(22);">Clear</button>` : ''}
          </div>
        </div>

        <h4 style="margin:0 0 8px 0;color:#fff;font-size:13px;">📊 Anonymized Northwest Alabama &amp; Remote Proof Case Studies</h4>
        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:10px;">
          ${G.CASE_STUDIES.map(
            (cs) => `
            <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:12px;">
              <div style="display:flex;justify-content:space-between;align-items:center;gap:6px;margin-bottom:4px;">
                <span style="font-family:var(--font-mono);font-size:10px;color:#34d399;font-weight:700;">${escapeHtml(cs.id)}</span>
                <span style="font-size:10px;color:var(--text-gold);">${escapeHtml(cs.sku)}</span>
              </div>
              <div style="font-size:12px;font-weight:800;color:#fff;margin-bottom:4px;">${escapeHtml(cs.headline)}</div>
              <div style="font-size:10px;color:#38bdf8;margin-bottom:6px;">${escapeHtml(cs.region)}</div>
              <div style="font-size:11px;color:var(--text-muted);margin-bottom:4px;"><strong>Before:</strong> ${escapeHtml(cs.beforeState)}</div>
              <div style="font-size:11px;color:var(--text-secondary);margin-bottom:6px;"><strong>Deliverable:</strong> ${escapeHtml(cs.deliverable)}</div>
              <div style="font-size:11px;color:#34d399;font-weight:700;">✓ ${escapeHtml(cs.verifiedOutcome)}</div>
            </div>
          `
          ).join('')}
        </div>
      `;
    } else {
      const checkState = G.getChecklistState();
      const doneCount = G.GBP_CHECKLIST_ITEMS.filter((i) => checkState[i.id]).length;
      const pct = Math.round((doneCount / G.GBP_CHECKLIST_ITEMS.length) * 100);
      bodyHtml = `
        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:14px;">
          <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;margin-bottom:10px;">
            <div>
              <h4 style="margin:0;color:#fff;">📍 Zero-Cost Google Business Profile &amp; Local SEO Checklist</h4>
              <p style="font-size:11px;color:var(--text-secondary);margin:2px 0 0 0;">Service-Area Business setup keeps residential addresses private while ranking across Northwest Alabama.</p>
            </div>
            <span style="font-family:var(--font-mono);font-size:13px;font-weight:800;color:#34d399;">${doneCount}/${G.GBP_CHECKLIST_ITEMS.length} (${pct}%)</span>
          </div>
          <div style="display:flex;flex-direction:column;gap:8px;">
            ${G.GBP_CHECKLIST_ITEMS.map(
              (item) => `
              <label style="display:flex;align-items:flex-start;gap:10px;background:#07070b;border:1px solid ${checkState[item.id] ? '#059669' : 'var(--border-subtle)'};border-radius:var(--radius-sm);padding:10px;cursor:pointer;">
                <input type="checkbox" ${checkState[item.id] ? 'checked' : ''} onchange="window.toggleGbpChecklist('${escapeHtml(item.id)}')" style="margin-top:3px;">
                <div style="flex:1;">
                  <div style="display:flex;justify-content:space-between;gap:8px;flex-wrap:wrap;">
                    <strong style="font-size:12px;color:#fff;">${escapeHtml(item.title)}</strong>
                    <span style="font-family:var(--font-mono);font-size:10px;color:#34d399;">${escapeHtml(item.cost)}</span>
                  </div>
                  <div style="font-size:11px;color:var(--text-secondary);margin-top:3px;line-height:1.4;">${escapeHtml(item.detail)}</div>
                </div>
              </label>
            `
            ).join('')}
          </div>
        </div>
      `;
    }

    card.innerHTML = `
      <div style="max-width:860px;margin:0 auto;">
        <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:14px;">
          <button class="filter-pill ${growthSubTab === 'QUIZ' ? 'active' : ''}" onclick="window.setGrowthSubTab('QUIZ')">⚡ 60s Audit Quiz</button>
          <button class="filter-pill ${growthSubTab === 'PITCH' ? 'active' : ''}" onclick="window.setGrowthSubTab('PITCH')">📣 Pitch &amp; Share</button>
          <button class="filter-pill ${growthSubTab === 'FLYER' ? 'active' : ''}" onclick="window.setGrowthSubTab('FLYER')">🖨️ QR Flyer &amp; Card</button>
          <button class="filter-pill ${growthSubTab === 'PROOF' ? 'active' : ''}" onclick="window.setGrowthSubTab('PROOF')">📊 Proof &amp; Referral</button>
          <button class="filter-pill ${growthSubTab === 'GBP' ? 'active' : ''}" onclick="window.setGrowthSubTab('GBP')">📍 GBP &amp; Local SEO</button>
        </div>
        ${bodyHtml}
      </div>
    `;
    container.appendChild(card);
  }

  window.setGrowthSubTab = function (tab) {
    growthSubTab = tab;
    renderPage(22);
  };

  window.setQuizAnswer = function (qId, idx) {
    quizAnswers[qId] = idx;
    window.TyrGrowth?.recordGrowthEvent('QUIZ', `Answered ${qId} option ${idx}`);
    renderPage(22);
  };

  window.applyQuizMatchesToCart = function () {
    const res = window.TyrGrowth.evaluateQuiz(quizAnswers);
    const all = getAllServices();
    const cart = window.TyrStripe.getCart();
    res.recommendedSkus.forEach((sku) => {
      const svc = all.find((s) => s.sku === sku);
      if (svc && !cart.some((c) => c.sku === sku)) cart.push(svc);
    });
    window.TyrStripe.saveCart(cart);
    window.TyrGrowth.recordGrowthEvent('QUIZ', `Routed ${res.recommendedSkus.join(', ')} to Quote Cart`);
    window.navigateToPage(12);
  };

  window.applyQuizMatchesToStripe = function () {
    const res = window.TyrGrowth.evaluateQuiz(quizAnswers);
    const all = getAllServices();
    const matched = res.recommendedSkus.map((sku) => all.find((s) => s.sku === sku)).filter(Boolean);
    if (matched.length) window.openStripeCheckoutModal(matched);
  };

  window.applyQuizMatchesToIntake = function () {
    const res = window.TyrGrowth.evaluateQuiz(quizAnswers);
    window.openClientIntakeModal(res.recommendedSkus[0] || 'SVC-WEB-01');
  };

  window.updatePitchChannel = function (ch) {
    pitchChannel = ch;
    renderPage(22);
  };

  window.updatePitchSku = function (sku) {
    pitchSku = sku;
    renderPage(22);
  };

  window.togglePitchRef = function (checked) {
    pitchIncludeRef = !!checked;
    renderPage(22);
  };

  window.copyPitchText = async function () {
    const txt = document.getElementById('growth-pitch-textarea')?.value || '';
    try {
      await navigator.clipboard.writeText(txt);
      window.TyrGrowth.recordGrowthEvent('SHARE', `Copied ${pitchChannel} pitch for ${pitchSku}`);
      alert('Copied pitch + deep link to clipboard!');
    } catch {
      alert('Select text inside the preview box to copy.');
    }
  };

  window.nativeSharePitch = async function () {
    const txt = document.getElementById('growth-pitch-textarea')?.value || '';
    const url = window.TyrGrowth.buildShareUrl(pitchSku, pitchIncludeRef);
    window.TyrGrowth.recordGrowthEvent('SHARE', `Dispatched native share (${pitchChannel}) for ${pitchSku}`);
    if (navigator.share) {
      try {
        await navigator.share({ title: `TYR Technical Consulting (${pitchSku})`, text: txt, url });
        return;
      } catch {}
    }
    window.copyPitchText();
  };

  window.setFlyerMode = function (mode) {
    flyerMode = mode;
    renderPage(22);
  };

  window.setFlyerTargetType = function (t) {
    flyerTargetType = t;
    renderPage(22);
  };

  window.setFlyerSku = function (sku) {
    flyerSku = sku;
    renderPage(22);
  };

  window.toggleFlyerRef = function (checked) {
    flyerIncludeRef = !!checked;
    renderPage(22);
  };

  function buildCurrentCollateralHtml() {
    const all = getAllServices();
    const svc = all.find((s) => s.sku === flyerSku) || all[0];
    const targetUrl =
      flyerTargetType === 'VCARD'
        ? window.TyrGrowth.MECARD_QR_STRING
        : flyerTargetType === 'SKU'
        ? window.TyrGrowth.buildShareUrl(svc?.sku || 'SVC-WEB-01', flyerIncludeRef)
        : window.TyrGrowth.buildShareUrl('', flyerIncludeRef);
    return window.TyrGrowth.buildPrintableCollateralHtml({
      mode: flyerMode,
      targetUrl,
      skuTitle: flyerTargetType === 'SKU' && svc ? svc.title : '',
      skuPrice: flyerTargetType === 'SKU' && svc ? svc.price : '',
      includeRef: flyerIncludeRef
    });
  }

  window.printQrCollateral = function () {
    const html = buildCurrentCollateralHtml();
    const w = window.open('', '_blank', 'width=860,height=920');
    if (w) {
      w.document.open();
      w.document.write(html);
      w.document.close();
      w.focus();
      setTimeout(() => w.print(), 250);
    } else {
      window.downloadQrCollateralHtml();
    }
  };

  window.downloadQrCollateralHtml = function () {
    const html = buildCurrentCollateralHtml();
    const name = flyerMode === 'CARD' ? 'tyr_leave_behind_card.html' : 'tyr_storefront_flyer.html';
    window.TyrStripe.downloadTextFile(name, html, 'text/html');
  };

  window.saveOwnRefCode = function () {
    const val = document.getElementById('own-ref-code-input')?.value || '';
    window.TyrGrowth.setOwnReferralCode(val);
    renderPage(22);
  };

  window.applyManualActiveRef = function () {
    const val = document.getElementById('active-ref-code-input')?.value || '';
    window.TyrGrowth.setActiveReferralCode(val);
    renderPage(22);
  };

  window.toggleGbpChecklist = function (id) {
    window.TyrGrowth.toggleChecklistItem(id);
    renderPage(22);
  };

  function getStoredOrders() {
    try {
      const data = localStorage.getItem('tyr_work_orders');
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  function saveOrder(order) {
    const orders = getStoredOrders();
    orders.unshift(order);
    localStorage.setItem('tyr_work_orders', JSON.stringify(orders));
  }

  function buildOrderFromFields(prefix) {
    const name = document.getElementById(`${prefix}-name`)?.value.trim() || '';
    const business = document.getElementById(`${prefix}-business`)?.value.trim() || '';
    const email = document.getElementById(`${prefix}-email`)?.value.trim() || '';
    const phone = document.getElementById(`${prefix}-phone`)?.value.trim() || '';
    const service = document.getElementById(`${prefix}-service`)?.value || 'SVC-WEB-01';
    const timeline = document.getElementById(`${prefix}-timeline`)?.value || 'Standard';
    const desc = document.getElementById(`${prefix}-desc`)?.value.trim() || '';
    const d = new Date();
    const dateStr = d.getFullYear() + String(d.getMonth() + 1).padStart(2, '0') + String(d.getDate()).padStart(2, '0');
    const randArr = new Uint8Array(1);
    window.crypto.getRandomValues(randArr);
    const orderNum = 10 + (randArr[0] % 90);
    const orderId = `WO-${dateStr}-${orderNum}`;
    return {
      id: orderId,
      clientName: name,
      businessName: business,
      email,
      phone,
      serviceSku: service,
      timeline,
      description: desc,
      timestamp: d.toLocaleString(),
      status: 'INTAKE_SUBMITTED'
    };
  }

  function renderIntakePage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';
    const all = getAllServices();
    const optionsHtml = all
      .map((s) => `<option value="${escapeHtml(s.sku)}">${escapeHtml(s.sku)}: ${escapeHtml(s.title)} (${escapeHtml(s.price)})</option>`)
      .join('');

    card.innerHTML = `
      <div style="max-width:640px;margin:0 auto;">
        <form id="inline-intake-form" onsubmit="window.handleIntakeFormSubmit(event)" style="display:flex;flex-direction:column;gap:12px;">
          <div>
            <label style="font-size:11px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Client Full Name *</label>
            <input type="text" id="intake-name" required placeholder="Jane Doe" style="width:100%;padding:10px;margin-top:4px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;">
          </div>
          <div>
            <label style="font-size:11px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Business / Organization</label>
            <input type="text" id="intake-business" placeholder="Acme Corp / Independent" style="width:100%;padding:10px;margin-top:4px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;">
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;">
            <div>
              <label style="font-size:11px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Email Address *</label>
              <input type="email" id="intake-email" required placeholder="client@example.com" style="width:100%;padding:10px;margin-top:4px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;">
            </div>
            <div>
              <label style="font-size:11px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Phone</label>
              <input type="tel" id="intake-phone" placeholder="(555) 000-0000" style="width:100%;padding:10px;margin-top:4px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;">
            </div>
          </div>
          <div>
            <label style="font-size:11px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Primary Service SKU</label>
            <select id="intake-service" style="width:100%;padding:10px;margin-top:4px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;">
              ${optionsHtml}
            </select>
          </div>
          <div>
            <label style="font-size:11px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Turnaround</label>
            <select id="intake-timeline" style="width:100%;padding:10px;margin-top:4px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;">
              <option value="Standard (7-14 Days)">Standard (7–14 Days)</option>
              <option value="Rush (48-72h)">Rush SLA (48–72h Priority)</option>
              <option value="Flexible">Flexible</option>
            </select>
          </div>
          <div>
            <label style="font-size:11px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Project Scope</label>
            <textarea id="intake-desc" rows="3" placeholder="Objectives or deadlines..." style="width:100%;padding:10px;margin-top:4px;border-radius:var(--radius-sm);background:var(--bg-attr);border:1px solid var(--border-subtle);color:#fff;font-size:13px;"></textarea>
          </div>
          <div style="display:flex;gap:10px;flex-wrap:wrap;">
            <button type="submit" class="btn-primary" style="flex:1;">Submit Work Order</button>
            <button type="button" onclick="window.handleIntakeChainSubmit()" class="btn-stripe" style="flex:1;justify-content:center;">⚡ Submit &amp; Chain to E-Sign + Stripe</button>
          </div>
        </form>
      </div>
    `;
    container.appendChild(card);
  }

  window.handleIntakeFormSubmit = function (e) {
    e.preventDefault();
    const order = buildOrderFromFields('intake');
    saveOrder(order);
    alert(`Work Order ${order.id} registered!`);
    window.navigateToPage(14);
  };

  window.handleIntakeChainSubmit = function () {
    const order = buildOrderFromFields('intake');
    if (!order.clientName || !order.email) {
      alert('Please enter Client Full Name and Email Address.');
      return;
    }
    saveOrder(order);
    chainedWorkOrder = order;
    window.navigateToPage(17);
  };

  window.openClientIntakeModal = function (preSku) {
    const modal = document.getElementById('intake-modal');
    if (!modal) {
      window.navigateToPage(13);
      return;
    }
    if (preSku) {
      const sel = document.getElementById('modal-intake-service');
      if (sel) sel.value = preSku;
    }
    modal.style.display = 'flex';
  };

  window.closeClientIntakeModal = function () {
    const modal = document.getElementById('intake-modal');
    if (modal) modal.style.display = 'none';
  };

  window.handleModalIntakeSubmit = function (e) {
    e.preventDefault();
    const order = buildOrderFromFields('modal-intake');
    saveOrder(order);
    window.closeClientIntakeModal();
    alert(`Work Order ${order.id} registered!`);
    window.navigateToPage(14);
  };

  window.handleModalIntakeChain = function () {
    const order = buildOrderFromFields('modal-intake');
    if (!order.clientName || !order.email) {
      alert('Please fill out Client Full Name and Email first.');
      return;
    }
    saveOrder(order);
    chainedWorkOrder = order;
    window.closeClientIntakeModal();
    window.navigateToPage(17);
  };

  function renderTrackerPage(container) {
    const orders = getStoredOrders();
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';

    const ordersHtml =
      orders.length === 0
        ? `<div style="text-align:center;padding:30px 10px;"><p style="color:var(--text-muted);font-size:13px;">No work orders submitted yet.</p><button onclick="navigateToPage(13)" class="btn-primary" style="margin-top:12px;">Launch Client Intake</button></div>`
        : orders
            .map(
              (o) => `
            <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:12px;margin-bottom:10px;">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-family:var(--font-mono);font-weight:700;color:var(--text-gold);font-size:12px;">${escapeHtml(o.id)}</span>
                <span style="background:rgba(5,150,105,0.2);color:#34d399;font-size:9px;font-weight:700;padding:2px 6px;border-radius:4px;">${escapeHtml(o.status)}</span>
              </div>
              <div style="margin-top:6px;font-size:13px;font-weight:600;color:#fff;">${escapeHtml(o.clientName)} ${o.businessName ? '• ' + escapeHtml(o.businessName) : ''}</div>
              <div style="font-size:11px;color:var(--text-secondary);margin-top:2px;">SKU: ${escapeHtml(o.serviceSku)} | Timeline: ${escapeHtml(o.timeline)}</div>
            </div>`
            )
            .join('');

    card.innerHTML = `<div style="max-width:720px;margin:0 auto;">${ordersHtml}</div>`;
    container.appendChild(card);
  }

  function renderContractSignPage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';

    const preName = chainedWorkOrder?.clientName || '';
    const preOrg = chainedWorkOrder?.businessName || '';
    const preEmail = chainedWorkOrder?.email || '';

    card.innerHTML = `
      <div style="max-width:720px;margin:0 auto;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;flex-wrap:wrap;gap:8px;">
          <div>
            <h3 style="margin:0;font-size:17px;color:#fff;">Digital Contract E-Sign (15 U.S.C. § 7001)</h3>
            <p style="margin:2px 0 0 0;font-size:11px;color:#34d399;">Real WebCrypto SHA-384 Digest • UETA Compliant</p>
          </div>
          <span style="font-size:10px;background:rgba(16,185,129,0.15);border:1px solid #10b981;padding:4px 8px;border-radius:4px;color:#34d399;">SHA-384 Seal</span>
        </div>

        <div style="background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:10px;margin-bottom:14px;">
          <label style="font-size:10px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Agreement Template</label>
          <select id="web-contract-doc" style="width:100%;padding:8px;margin-top:4px;font-size:12px;background:#07070b;border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
            <option value="DOC-001_WO-2026-1028-01">DOC-001_WO-2026: Master Services Agreement &amp; Statement of Work</option>
            <option value="DOC-001_WO-2026-1028-02">DOC-001_RET-2026: Executive Architecture Retainer Agreement</option>
            <option value="DOC-002_NDA-2026-0001">DOC-002_NDA-2026: Mutual NDA &amp; Zero-Custody Covenant</option>
          </select>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:14px;">
          <div>
            <label style="font-size:10px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Signer Legal Name *</label>
            <input type="text" id="web-signer-name" value="${escapeHtml(preName)}" placeholder="Jane Doe" style="width:100%;padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
          </div>
          <div>
            <label style="font-size:10px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Organization</label>
            <input type="text" id="web-signer-org" value="${escapeHtml(preOrg)}" placeholder="Acme Systems" style="width:100%;padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
          </div>
          <div>
            <label style="font-size:10px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Signer Title</label>
            <input type="text" id="web-signer-title" value="Authorized Representative" style="width:100%;padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
          </div>
          <div>
            <label style="font-size:10px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Official Email *</label>
            <input type="email" id="web-signer-email" value="${escapeHtml(preEmail)}" placeholder="signer@example.com" style="width:100%;padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
          </div>
        </div>

        <div style="margin-bottom:12px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4px;">
            <label style="font-size:10px;text-transform:uppercase;color:var(--text-gold);font-weight:700;">Touchscreen Signature Pad</label>
            <button type="button" onclick="window.clearWebSignature()" class="btn-secondary" style="font-size:10px;padding:2px 8px;color:#f87171;">Clear</button>
          </div>
          <div style="background:#07070b;border:1px solid var(--border-subtle);border-radius:var(--radius-sm);height:120px;position:relative;">
            <canvas id="web-sig-canvas" width="680" height="120" style="width:100%;height:100%;touch-action:none;cursor:crosshair;"></canvas>
            <div id="web-sig-placeholder" style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;pointer-events:none;color:var(--text-muted);font-size:11px;">
              ✍️ Draw signature with mouse, finger, or stylus
            </div>
          </div>
        </div>

        <label style="display:flex;align-items:flex-start;gap:8px;font-size:10px;color:#94a3b8;margin-bottom:12px;cursor:pointer;">
          <input type="checkbox" id="web-sign-consent" checked style="margin-top:2px;">
          <span>I verify legal authority under 15 U.S.C. § 7001 and Alabama UETA. Zero-Custody Covenant acknowledged.</span>
        </label>

        <button type="button" onclick="window.executeWebContract()" class="btn-primary" style="background:#10b981;width:100%;padding:12px;">
          🔒 Execute &amp; Seal Agreement (Compute WebCrypto SHA-384)
        </button>

        <div id="web-cert-result" style="display:none;margin-top:14px;background:#0f172a;border:1px solid #10b981;border-radius:var(--radius-sm);padding:12px;"></div>
      </div>
    `;
    container.appendChild(card);
    window.initWebSignatureCanvas();
  }

  window.initWebSignatureCanvas = function () {
    const canvas = document.getElementById('web-sig-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let drawing = false;
    ctx.strokeStyle = '#38bdf8';
    ctx.lineWidth = 2.5;
    ctx.lineCap = 'round';

    function getPos(e) {
      const rect = canvas.getBoundingClientRect();
      const cx = e.touches ? e.touches[0].clientX : e.clientX;
      const cy = e.touches ? e.touches[0].clientY : e.clientY;
      return { x: ((cx - rect.left) * canvas.width) / rect.width, y: ((cy - rect.top) * canvas.height) / rect.height };
    }
    const start = (e) => {
      drawing = true;
      document.getElementById('web-sig-placeholder')?.style.setProperty('display', 'none');
      const p = getPos(e);
      ctx.beginPath();
      ctx.moveTo(p.x, p.y);
      e.preventDefault();
    };
    const move = (e) => {
      if (!drawing) return;
      const p = getPos(e);
      ctx.lineTo(p.x, p.y);
      ctx.stroke();
      e.preventDefault();
    };
    const end = () => {
      drawing = false;
    };
    canvas.addEventListener('mousedown', start);
    canvas.addEventListener('mousemove', move);
    window.addEventListener('mouseup', end);
    canvas.addEventListener('touchstart', start, { passive: false });
    canvas.addEventListener('touchmove', move, { passive: false });
    window.addEventListener('touchend', end);
  };

  window.clearWebSignature = function () {
    const canvas = document.getElementById('web-sig-canvas');
    if (!canvas) return;
    canvas.getContext('2d').clearRect(0, 0, canvas.width, canvas.height);
    document.getElementById('web-sig-placeholder')?.style.setProperty('display', 'flex');
  };

  window.executeWebContract = async function () {
    const name = document.getElementById('web-signer-name')?.value.trim();
    const email = document.getElementById('web-signer-email')?.value.trim();
    const org = document.getElementById('web-signer-org')?.value.trim() || 'Independent';
    const title = document.getElementById('web-signer-title')?.value.trim() || 'Representative';
    const docCode = document.getElementById('web-contract-doc')?.value;
    const consent = document.getElementById('web-sign-consent')?.checked;

    if (!name || !email) {
      alert('Please provide signer legal name and official email.');
      return;
    }
    if (!consent) {
      alert('Please check the statutory consent box.');
      return;
    }

    const timestamp = new Date().toISOString();
    const sha384Seal = await window.TyrStripe.sha384Hex(`${docCode}|${name}|${org}|${email}|${timestamp}`);
    const certBox = document.getElementById('web-cert-result');
    const targetSku = chainedWorkOrder?.serviceSku || 'SVC-WEB-01';

    if (certBox) {
      certBox.style.display = 'block';
      certBox.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <span style="color:#10b981;font-weight:700;font-size:11px;">✓ EXECUTED CONTRACT CERTIFICATE</span>
          <span style="font-family:var(--font-mono);font-size:10px;color:var(--text-gold);">${escapeHtml(docCode)}</span>
        </div>
        <div style="font-size:12px;font-weight:600;color:#fff;">${escapeHtml(name)} (${escapeHtml(title)}) • ${escapeHtml(org)}</div>
        <div style="font-size:10px;color:var(--text-secondary);margin-top:2px;">Email: ${escapeHtml(email)} • ${escapeHtml(timestamp)}</div>
        <div style="background:#020617;border:1px solid #1e293b;border-radius:4px;padding:6px 8px;margin-top:8px;">
          <div style="font-size:9px;color:#94a3b8;font-weight:700;">WEBCRYPTO SHA-384 FINGERPRINT:</div>
          <div style="font-family:var(--font-mono);font-size:9px;color:#38bdf8;word-break:break-all;">${escapeHtml(sha384Seal)}</div>
        </div>
        <div style="margin-top:10px;display:flex;gap:8px;flex-wrap:wrap;">
          <button type="button" class="btn-stripe" style="flex:1;justify-content:center;" onclick="window.openStripeForSku('${escapeHtml(targetSku)}')">
            💳 Step 3: Proceed to Stripe Deposit / Full Settlement
          </button>
          <a href="mailto:tyr@tyrprotocol.pro?subject=Executed%20Contract%20[${escapeHtml(docCode)}]&body=SHA-384%20Seal:%20${escapeHtml(sha384Seal)}" class="btn-primary" style="padding:8px 12px;font-size:11px;">Email Seal</a>
        </div>
      `;
    }
  };

  function renderZeroCustodyVaultPage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';
    card.innerHTML = `
      <div style="max-width:720px;margin:0 auto;">
        <h3 style="margin:0 0 8px 0;">🛡️ Zero-Custody Document Vault &amp; Statement Sanitizer</h3>
        <p style="font-size:12px;color:var(--text-secondary);margin-bottom:10px;">Paste any statement or log to scrub payment cards, SSNs, and API tokens in memory:</p>
        <textarea id="vault-sanitize-input" rows="4" style="width:100%;padding:10px;font-size:11px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;margin-bottom:8px;">Card 4532-1111-2222-3333
SSN 123-45-6789
Monthly Cloud Fee: $185.00</textarea>
        <button onclick="window.runVaultSanitizer()" class="btn-primary" style="background:#0f766e;width:100%;padding:10px;margin-bottom:10px;">🛡️ Sanitize Under Zero-Custody Rule</button>
        <div id="vault-sanitized-output" style="display:none;background:#064e3b;border:1px solid #059669;border-radius:var(--radius-sm);padding:10px;"></div>
      </div>
    `;
    container.appendChild(card);
  }

  function scrubZeroCustody(rawText) {
    let text = String(rawText || '');
    const redactions = [];
    const cardRe = /\b(?:\d[ -]*?){13,19}\b/g;
    if (cardRe.test(text)) {
      redactions.push('Payment Card Number');
      text = text.replace(cardRe, '[REDACTED_PAYMENT_CARD]');
    }
    const ssnRe = /\b\d{3}-\d{2}-\d{4}\b/g;
    if (ssnRe.test(text)) {
      redactions.push('SSN');
      text = text.replace(ssnRe, '[REDACTED_SSN]');
    }
    const secRe = /\b(?:password|passwd|secret|token|bearer|api[_-]?key)\s*[:=]\s*\S+/gi;
    if (secRe.test(text)) {
      redactions.push('Credential');
      text = text.replace(secRe, '[REDACTED_CREDENTIAL]');
    }
    return { text, redactions };
  }

  window.runVaultSanitizer = function () {
    const raw = document.getElementById('vault-sanitize-input')?.value || '';
    const { text, redactions } = scrubZeroCustody(raw);
    const outBox = document.getElementById('vault-sanitized-output');
    if (outBox) {
      outBox.style.display = 'block';
      outBox.innerHTML = `<div style="font-size:10px;color:#a7f3d0;font-weight:700;margin-bottom:4px;">✓ SANITIZED (${redactions.length} Redactions)</div><pre style="font-family:var(--font-mono);font-size:11px;color:#fff;margin:0;white-space:pre-wrap;">${escapeHtml(text)}</pre>`;
    }
  };

  function renderBookingPage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';
    card.innerHTML = `
      <div style="max-width:640px;margin:0 auto;">
        <h3 style="margin:0 0 10px 0;">📅 Bifröst Strategy Call Scheduler</h3>
        <form onsubmit="window.handleWebBooking(event)" style="display:flex;flex-direction:column;gap:10px;">
          <select id="booking-duration" style="padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
            <option value="15-Min Discovery Call">15-Min Rapid Scoping Call ($0 Obligation)</option>
            <option value="30-Min Architecture Review">30-Min Technical Architecture Deep Dive</option>
          </select>
          <input type="text" id="booking-name" required placeholder="Client Full Name *" style="padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
          <input type="tel" id="booking-phone" required placeholder="Phone Number *" style="padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
          <select id="booking-slot" style="padding:8px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
            <option value="Tomorrow 10:00 AM CST">Tomorrow 10:00 AM CST (Alabama Time)</option>
            <option value="Tomorrow 02:00 PM CST">Tomorrow 02:00 PM CST</option>
          </select>
          <button type="submit" class="btn-primary" style="padding:11px;">Confirm Strategy Call &amp; Dispatch Email</button>
        </form>
      </div>
    `;
    container.appendChild(card);
  }

  window.handleWebBooking = function (e) {
    e.preventDefault();
    const dur = document.getElementById('booking-duration')?.value || '';
    const name = document.getElementById('booking-name')?.value || '';
    const phone = document.getElementById('booking-phone')?.value || '';
    const slot = document.getElementById('booking-slot')?.value || '';
    window.location.href = `mailto:tyr@tyrprotocol.pro?subject=${encodeURIComponent(`Bifröst Booking: ${dur} - ${name}`)}&body=${encodeURIComponent(`Client: ${name}\nPhone: ${phone}\nSlot: ${slot}`)}`;
  };

  function renderImageForgePage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';
    card.innerHTML = `
      <div style="max-width:720px;margin:0 auto;">
        <h3 style="margin:0 0 10px 0;">🎨 Runic Image Forge: Týr &amp; Freyja</h3>
        <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px;margin-bottom:12px;">
          <button id="forge-btn-tyr" class="btn-primary" onclick="window.selectForgeDeity('tyr')">ᛏ Týr</button>
          <button id="forge-btn-freya" class="btn-secondary" onclick="window.selectForgeDeity('freya')">ᚠ Freyja</button>
          <button id="forge-btn-syncretic" class="btn-secondary" onclick="window.selectForgeDeity('syncretic')">ᛏ ᚠ Alliance</button>
        </div>
        <textarea id="forge-prompt-output" rows="4" readonly style="width:100%;padding:10px;font-family:var(--font-mono);font-size:11px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:var(--text-secondary);margin-bottom:10px;"></textarea>
        <button onclick="navigator.clipboard.writeText(document.getElementById('forge-prompt-output').value).then(()=>alert('Copied prompt!'))" class="btn-primary">📋 Copy Prompt</button>
      </div>
    `;
    container.appendChild(card);
    window.selectForgeDeity('tyr');
  }

  window.selectForgeDeity = function (key) {
    const prompts = {
      tyr: 'The Norse god Týr (Tiwaz), sovereign of divine law and kept oaths, centered with rune ᛏ, dramatic chiaroscuro oil painting, dark basalt & crimson gold lighting.',
      freya: 'The Norse goddess Freyja (Fehu), Lady of Fólkvangr, adorned with Brísingamen golden torc and falcon cloak, rune ᚠ, emerald & warm gold oil painting.',
      syncretic: 'Grand alliance of Týr (ᛏ) and Freyja (ᚠ), scales of justice balanced over golden harvest beneath northern lights, masterwork chiaroscuro dark fantasy.'
    };
    const out = document.getElementById('forge-prompt-output');
    if (out) out.value = prompts[key] || prompts.tyr;
  };

  function renderGoogleChatRelayPage(container) {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.display = 'block';
    card.style.gridColumn = '1 / -1';
    const savedWebhook = localStorage.getItem('tyr_gchat_webhook') || '';
    card.innerHTML = `
      <div style="max-width:720px;margin:0 auto;">
        <h3 style="margin:0 0 8px 0;">📡 Google Chat Relay (Zero-Custody Scrubbed)</h3>
        <input type="url" id="relay-webhook-url" value="${escapeHtml(savedWebhook)}" placeholder="https://chat.googleapis.com/v1/spaces/.../messages?key=..." style="width:100%;padding:8px;font-size:11px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;margin-bottom:8px;">
        <button onclick="localStorage.setItem('tyr_gchat_webhook', document.getElementById('relay-webhook-url').value.trim()); alert('Saved webhook URL');" class="btn-secondary" style="font-size:10px;padding:4px 10px;margin-bottom:10px;">Save Webhook</button>
        <div style="display:flex;gap:8px;">
          <input type="text" id="relay-input" placeholder="Type message for management relay..." style="flex:1;padding:9px;font-size:12px;background:var(--bg-attr);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);color:#fff;">
          <button onclick="window.sendRelayMessage()" class="btn-primary" style="background:#0284c7;">Send</button>
        </div>
        <div id="relay-log-box" style="margin-top:10px;font-size:11px;color:#34d399;"></div>
      </div>
    `;
    container.appendChild(card);
  }

  window.sendRelayMessage = function () {
    const inp = document.getElementById('relay-input');
    if (!inp || !inp.value.trim()) return;
    const { text, redactions } = scrubZeroCustody(inp.value.trim());
    inp.value = '';
    const log = document.getElementById('relay-log-box');
    if (log) log.textContent = `✓ Scrubbed (${redactions.length} redactions): ${text}`;
  };

  window.stepCard = function (direction) {
    if (!catalogData || currentPage > 9) return;
    const pageObj = catalogData.pages.find((p) => p.page === currentPage);
    if (!pageObj || !pageObj.services) return;
    const total = pageObj.services.length;
    let next = activeCardIndex + direction;
    if (next >= total) {
      if (currentPage < 9) {
        currentPage++;
        activeCardIndex = 0;
        renderPage(currentPage);
      }
      return;
    }
    if (next < 0) {
      if (currentPage > 1) {
        currentPage--;
        const prev = catalogData.pages.find((p) => p.page === currentPage);
        activeCardIndex = prev?.services ? prev.services.length - 1 : 0;
        renderPage(currentPage);
      }
      return;
    }
    activeCardIndex = next;
    document.querySelectorAll('#deck-grid .card').forEach((c, idx) => {
      c.classList.toggle('mobile-active', idx === activeCardIndex);
    });
    const mobileCounter = document.getElementById('mobile-card-counter');
    if (mobileCounter) mobileCounter.textContent = `Card ${activeCardIndex + 1} of ${total}`;
  };

  function setupTouchSwipe() {
    const deck = document.getElementById('deck-grid');
    if (!deck || deck.dataset.swipeBound === '1') return;
    deck.dataset.swipeBound = '1';
    deck.addEventListener('touchstart', (e) => {
      touchStartX = e.changedTouches[0].screenX;
      touchStartY = e.changedTouches[0].screenY;
    }, { passive: true });
    deck.addEventListener('touchend', (e) => {
      const dx = e.changedTouches[0].screenX - touchStartX;
      const dy = e.changedTouches[0].screenY - touchStartY;
      if (Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(dy)) {
        window.stepCard(dx < 0 ? 1 : -1);
      }
    }, { passive: true });
  }

  window.closeOrderModal = function () {
    const m = document.getElementById('order-modal');
    if (m) m.style.display = 'none';
  };

  document.addEventListener('DOMContentLoaded', loadCatalog);
})(); 
window.openCookieConsentBanner = function () {
  var b = document.getElementById('cookie-consent-banner');
  if (b) b.hidden = false;
  };
window.setCookieConsent = function (choice) {
  try { window.localStorage.setItem('tyr_gtag_consent', choice); } catch (e) {}
  if (typeof window.gtag === 'function') {
    window.gtag('consent', 'update', { ad_storage: choice, analytics_storage: choice, ad_user_data: choice, ad_personalization: choice });
  }
  var b = document.getElementById('cookie-consent-banner');
  if (b) b.hidden = true;
  };
try {
  if (!window.localStorage.getItem('tyr_gtag_consent')) {
    var cb = document.getElementById('cookie-consent-banner');
    if (cb) cb.hidden = false;
  }
  } catch (e) {}

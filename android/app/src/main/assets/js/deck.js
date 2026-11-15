/**
 * TYR Protocol — Deck Controller & Touch Stepper
 */

(function () {
  'use strict';

  let catalogData = null;
  let currentPage = 1;
  let activeCardIndex = 0;
  let touchStartX = 0;
  let touchStartY = 0;

  // Auto-detect mobile device or APK environment
  const isMobileScreen = window.innerWidth <= 768 || navigator.maxTouchPoints > 0;
  const isAndroidAPK = navigator.userAgent.includes('TyrProtocolAndroidAPK');

  if (isMobileScreen || isAndroidAPK) {
    document.documentElement.setAttribute('data-device', 'mobile');
  }

  // Load catalog.json
  async function loadCatalog() {
    try {
      const res = await fetch('./data/catalog.json');
      catalogData = await res.json();
      renderPage(currentPage);
      setupStepperTabs();
      setupTouchSwipe();
    } catch (e) {
      console.error('Failed to load catalog:', e);
    }
  }

  function setupStepperTabs() {
    const tabs = document.querySelectorAll('.step-tab');
    tabs.forEach((tab) => {
      tab.addEventListener('click', (e) => {
        e.preventDefault();
        const p = parseInt(tab.getAttribute('data-page'), 10);
        if (p && p !== currentPage) {
          currentPage = p;
          activeCardIndex = 0;
          renderPage(currentPage);
        }
      });
    });
  }

  function renderPage(pageNumber) {
    if (!catalogData) return;

    // Update active tab in header
    document.querySelectorAll('.step-tab').forEach((tab) => {
      const p = parseInt(tab.getAttribute('data-page'), 10);
      tab.classList.toggle('active', p === pageNumber);
    });

    const pageObj = catalogData.pages.find((p) => p.page === pageNumber);
    if (!pageObj) return;

    const pageHeaderTitle = document.getElementById('page-title');
    const pageHeaderSub = document.getElementById('page-subtitle');
    const deckGrid = document.getElementById('deck-grid');
    const mobileCounter = document.getElementById('mobile-card-counter');

    if (pageHeaderTitle) pageHeaderTitle.textContent = pageObj.title;
    if (pageHeaderSub) pageHeaderSub.textContent = pageObj.subtitle;

    if (!deckGrid) return;
    deckGrid.innerHTML = '';

    // If Page 7: Workflow & Zero Custody
    if (pageNumber === 7) {
      renderPageSeven(pageObj, deckGrid);
      if (mobileCounter) mobileCounter.textContent = 'Bifröst 3-Step Protocol';
      updateMobileActionButtons('BIFROST-ENGAGE', '$0 Initiation Call');
      return;
    }

    // Render Service Cards
    const services = pageObj.services || [];
    services.forEach((svc, index) => {
      const card = document.createElement('article');
      card.className = `card ${index === activeCardIndex ? 'mobile-active' : ''}`;
      card.setAttribute('data-sku', svc.sku);

      // 3 Bullets
      const bulletsHtml = svc.bullets
        .map((b) => `<li>${escapeHtml(b)}</li>`)
        .join('');

      card.innerHTML = `
        <div class="card-top">
          <div class="card-top-left">
            <span class="rune-pill">${escapeHtml(svc.rune)}</span>
            <span class="code-tag">${escapeHtml(svc.sku)}</span>
            <span class="realm-meaning">${escapeHtml(svc.realm)}</span>
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
        <ul class="card-bullets">
          ${bulletsHtml}
        </ul>
        <div class="card-footer">
          <div class="badge-group">
            <span class="badge">${escapeHtml(svc.badge)}</span>
            <span class="spec-tag">${escapeHtml(svc.spec)}</span>
          </div>
          <button class="btn-primary order-trigger" data-sku="${escapeHtml(svc.sku)}" data-title="${escapeHtml(svc.title)}" data-price="${escapeHtml(svc.price)}">
            Order SKU
          </button>
        </div>
      `;

      deckGrid.appendChild(card);
    });

    // Update active mobile buttons
    const activeSvc = services[activeCardIndex] || services[0];
    if (activeSvc) {
      updateMobileActionButtons(activeSvc.sku, activeSvc.price);
    }

    if (mobileCounter) {
      mobileCounter.textContent = `Card ${activeCardIndex + 1} of ${services.length}`;
    }

    // Attach order trigger listeners
    document.querySelectorAll('.order-trigger').forEach((btn) => {
      btn.addEventListener('click', (e) => {
        const sku = btn.getAttribute('data-sku');
        const title = btn.getAttribute('data-title');
        const price = btn.getAttribute('data-price');
        openOrderModal(sku, title, price);
      });
    });
  }

  function renderPageSeven(pageObj, container) {
    const workflowGrid = document.createElement('div');
    workflowGrid.className = 'workflow-grid';

    (pageObj.workflow || []).forEach((wf) => {
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
        <a href="tel:+16592578043" class="btn-primary btn-call">Direct Call (659) 257-8043</a>
        <a href="mailto:tyr@tyrprotocol.pro?subject=Engagement%20Inquiry%20under%20Bifrost%20Protocol" class="btn-secondary">Email Dispatch</a>
      </div>
    `;
    container.appendChild(zeroBox);
  }

  function updateMobileActionButtons(sku, price) {
    const mobBtn = document.getElementById('mobile-order-sku');
    if (mobBtn) {
      mobBtn.textContent = `Order ${sku} (${price})`;
      mobBtn.onclick = () => {
        openOrderModal(sku, `Order ${sku}`, price);
      };
    }
  }

  // Mobile Stepper Navigation (Previous / Next Card)
  window.stepCard = function (direction) {
    if (!catalogData) return;
    const pageObj = catalogData.pages.find((p) => p.page === currentPage);
    if (!pageObj || !pageObj.services) return;

    const totalCards = pageObj.services.length;
    let newIndex = activeCardIndex + direction;

    if (newIndex >= totalCards) {
      // Step to next page
      if (currentPage < 7) {
        currentPage++;
        activeCardIndex = 0;
        renderPage(currentPage);
      }
      return;
    } else if (newIndex < 0) {
      // Step to prev page
      if (currentPage > 1) {
        currentPage--;
        const prevPageObj = catalogData.pages.find((p) => p.page === currentPage);
        activeCardIndex = (prevPageObj && prevPageObj.services) ? prevPageObj.services.length - 1 : 0;
        renderPage(currentPage);
      }
      return;
    }

    activeCardIndex = newIndex;
    const cards = document.querySelectorAll('#deck-grid .card');
    cards.forEach((c, idx) => {
      c.classList.toggle('mobile-active', idx === activeCardIndex);
    });

    const activeSvc = pageObj.services[activeCardIndex];
    if (activeSvc) {
      updateMobileActionButtons(activeSvc.sku, activeSvc.price);
    }

    const mobileCounter = document.getElementById('mobile-card-counter');
    if (mobileCounter) {
      mobileCounter.textContent = `Card ${activeCardIndex + 1} of ${totalCards}`;
    }
  };

  // Touch Swipe Gesture Handler
  function setupTouchSwipe() {
    const deck = document.getElementById('deck-grid');
    if (!deck) return;

    deck.addEventListener('touchstart', (e) => {
      touchStartX = e.changedTouches[0].screenX;
      touchStartY = e.changedTouches[0].screenY;
    }, { passive: true });

    deck.addEventListener('touchend', (e) => {
      const touchEndX = e.changedTouches[0].screenX;
      const touchEndY = e.changedTouches[0].screenY;
      const diffX = touchEndX - touchStartX;
      const diffY = touchEndY - touchStartY;

      // Ensure horizontal swipe is dominant
      if (Math.abs(diffX) > 40 && Math.abs(diffX) > Math.abs(diffY)) {
        if (diffX < 0) {
          // Swipe Left -> Next Card
          window.stepCard(1);
        } else {
          // Swipe Right -> Prev Card
          window.stepCard(-1);
        }
      }
    }, { passive: true });
  }

  // Order Modal
  function openOrderModal(sku, title, price) {
    const modal = document.getElementById('order-modal');
    const modalSku = document.getElementById('modal-sku');
    const modalTitle = document.getElementById('modal-title');
    const modalPrice = document.getElementById('modal-price');
    const modalEmailLink = document.getElementById('modal-email-btn');
    const modalSmsLink = document.getElementById('modal-sms-btn');

    if (modalSku) modalSku.textContent = sku;
    if (modalTitle) modalTitle.textContent = title;
    if (modalPrice) modalPrice.textContent = price;

    const emailSubject = encodeURIComponent(`Engagement Order: [${sku}] ${title}`);
    const emailBody = encodeURIComponent(
      `Hello Caleb TR Holcomb d/b/a TYR,\n\nI am requesting engagement for SKU: ${sku} (${price}).\n\nProject Scope & Timeline:\n\nContact Name:\nOrganization:\nPhone Number:\n`
    );

    if (modalEmailLink) {
      modalEmailLink.href = `mailto:tyr@tyrprotocol.pro?subject=${emailSubject}&body=${emailBody}`;
    }

    const smsBody = encodeURIComponent(`TYR Protocol Order: [${sku}] ${title}`);
    if (modalSmsLink) {
      modalSmsLink.href = `sms:+16592578043?body=${smsBody}`;
    }

    if (modal) modal.style.display = 'flex';
  }

  window.closeOrderModal = function () {
    const modal = document.getElementById('order-modal');
    if (modal) modal.style.display = 'none';
  };

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // Initialize
  document.addEventListener('DOMContentLoaded', loadCatalog);
})();

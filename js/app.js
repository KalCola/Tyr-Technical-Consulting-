// File: js/app.js
/**
 * TYR Protocol — Application Bootstrap & PWA Modal Listeners
 */
(function () {
  'use strict';

  // Touch support is common on laptops and monitors, so it must not by itself
  // force a desktop browser into the mobile, single-card layout.
  function syncDeviceMode() {
    const userAgent = navigator.userAgent || '';
    const isMobileUserAgent = /Android|iPhone|iPad|iPod|Mobile|TyrProtocolAndroidAPK/i.test(userAgent);
    const isMobileViewport = window.matchMedia('(max-width: 768px)').matches;
    const device = isMobileUserAgent || isMobileViewport ? 'mobile' : 'desktop';

    document.documentElement.setAttribute('data-device', device);
    if (document.body) document.body.setAttribute('data-device', device);
  }

  syncDeviceMode();
  window.addEventListener('resize', syncDeviceMode, { passive: true });
  window.addEventListener('orientationchange', syncDeviceMode, { passive: true });

  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('./sw.js').catch((err) => {
        console.log('SW registration skipped:', err);
      });
    });
  }

  window.toggleLegalModal = function (show) {
    const modal = document.getElementById('legal-modal');
    if (modal) {
      modal.style.display = show ? 'flex' : 'none';
    }
  };

  window.addEventListener('click', (e) => {
    const modalIds = [
      'order-modal',
      'legal-modal',
      'intake-modal',
      'catalog-sync-modal',
      'category-drawer-modal',
      'stripe-checkout-modal'
    ];
    modalIds.forEach((id) => {
      const el = document.getElementById(id);
      if (el && e.target === el) {
        el.style.display = 'none';
      }
    });
  });

  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      window.closeOrderModal?.();
      window.toggleLegalModal?.(false);
      window.closeClientIntakeModal?.();
      window.closeCatalogSyncModal?.();
      window.closeCategoryDrawer?.();
      window.closeStripeCheckoutModal?.();
    }
  });
})();

/**
 * TYR Protocol — Application Bootstrap & PWA
 */

(function () {
  'use strict';

  // Register Service Worker for offline capability
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('./sw.js').catch((err) => {
        console.log('SW registration skipped:', err);
      });
    });
  }

  // Legal and Disclaimer Drawer
  window.toggleLegalModal = function (show) {
    const modal = document.getElementById('legal-modal');
    if (modal) {
      modal.style.display = show ? 'flex' : 'none';
    }
  };

  // Close modals on backdrop click
  window.addEventListener('click', (e) => {
    const orderModal = document.getElementById('order-modal');
    const legalModal = document.getElementById('legal-modal');
    if (e.target === orderModal) {
      orderModal.style.display = 'none';
    }
    if (e.target === legalModal) {
      legalModal.style.display = 'none';
    }
  });

  // ESC key to close modals
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      window.closeOrderModal();
      window.toggleLegalModal(false);
    }
  });
})();

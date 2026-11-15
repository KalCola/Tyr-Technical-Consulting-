// File: sw.js
const CACHE_NAME = 'tyr-protocol-v4.2';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './404.html',
  './manifest.webmanifest',
  './css/base.css',
  './css/deck.css',
  './css/responsive.css',
  './js/stripe-checkout.js',
  './js/forensics.js',
  './js/growth-kit.js',
  './js/deck.js',
  './js/app.js',
  './data/catalog.json',
  './assets/3d_card.html',
  './assets/apple-touch-icon.png',
  './assets/logo-512.jpg',
  './assets/favicon.ico'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.addAll(ASSETS_TO_CACHE))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) =>
        Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key)))
      )
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      if (cachedResponse) {
        return cachedResponse;
      }
      return fetch(event.request)
        .then((networkResponse) => {
          if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
            return networkResponse;
          }
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, responseToCache);
          });
          return networkResponse;
        })
        .catch(() => {
          if (event.request.mode === 'navigate') {
            return caches.match('./index.html');
          }
        });
    })
  );
});

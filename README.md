# TYR Technical Consulting — Nine Realms Storefront

Static, build-free site for **https://tyrprotocol.pro**, hosted on GitHub Pages (`main` branch, repository root).
Open `index.html` through any static server to preview (`python3 -m http.server 8000`, then visit `http://localhost:8000`).
[![GitHub Pages Verification](https://github.com/KalCola/Tyr-Technical-Consulting-/actions/workflows/pages-verify.yml/badge.svg)](https://github.com/KalCola/Tyr-Technical-Consulting-/actions/workflows/pages-verify.yml)
[![Live Storefront](https://img.shields.io/badge/site-tyrprotocol.pro-blue.svg)](https://tyrprotocol.pro)

## File map

| File                                            | What it is                                                                                                                         | Edit it when…                                                                   |
| ----------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| `index.html`                                    | The whole storefront markup: head/SEO, consent banner, nav, hero, six deck pages, footer.                                          | Changing copy, prices, cards, links.                                            |
| `css/base.css`                                  | Design tokens (Nine Realms palette), reset, nav, UA-CH bar, skip link, focus ring.                                                 | Changing colors, fonts, global look.                                            |
| `css/components.css`                            | Hero, tabs, realm legend, cards, bundle banner, workflow, pager, cookie banner.                                                    | Changing a component's look.                                                    |
| `css/responsive.css`                            | Tablet/mobile/forced-desktop rules (`html[data-device]`), reduced motion. Must load last.                                          | Fixing a layout at a screen size.                                               |
| `js/device-init.js`                             | Sync, tiny: sets `data-device` before first paint (no desktop flash on phones).                                                    | Rarely.                                                                         |
| `js/consent-init.js`                            | Google Consent Mode v2 defaults (denied), restores a saved "granted", starts the Ads tag. Runs before `gtag.js`.                   | Changing the Ads/Analytics ID.                                                  |
| `js/deck.js`                                    | Tabs, `#page-N` / `#all` deep links + back button, mobile one-card stepper, swipe, keyboard, all click handling via `data-action`. | Changing navigation behavior.                                                   |
| `js/client-hints.js`                            | User-Agent Client Hints detection, Auto/Desktop/Mobile override, telemetry pill.                                                   | Changing device detection.                                                      |
| `js/analytics.js`                               | Consent banner logic, "Cookie settings" link, phone & email conversion tracking (`AW-18518856067`).                                | Changing consent/conversion behavior.                                           |
| `js/catalog-loader.js`                          | Syncs any price, title, badge, or spec changes from `data/catalog.json` into the DOM by `data-sku`.                                | Rarely.                                                                         |
| `data/catalog.json`                             | Single JSON source of truth for all 15 service SKUs, bundle pricing, 9-Realm hex tokens, and contact telemetry.                    | Updating prices, titles, badges, or contact info without touching `index.html`. |
| `assets/logo-120.webp`, `logo-240.webp`         | Hero emblem at 1x / 2x (3 KB / 10 KB).                                                                                             | Replacing the logo.                                                             |
| `assets/logo-512.jpg`                           | Logo referenced by the JSON-LD schema.                                                                                             | Replacing the logo.                                                             |
| `assets/og-image.jpg`                           | 1200×630 social-share card.                                                                                                        | Replacing the logo.                                                             |
| `assets/favicon-32.png`, `apple-touch-icon.png` | Browser tab / iOS home-screen icons.                                                                                               | Replacing the logo.                                                             |
| `404.html`                                      | Real 404 page (served with HTTP 404, `noindex`). Self-contained, absolute URLs.                                                    | Rarely.                                                                         |
| `robots.txt`, `sitemap.xml`                     | Crawler rules and the one-URL sitemap. Update `lastmod` on content changes.                                                        | Releases.                                                                       |
| `CNAME`                                         | Custom domain `tyrprotocol.pro`. Do not delete.                                                                                    | Never.                                                                          |
| `.nojekyll`                                     | Tells GitHub Pages to serve files as-is.                                                                                           | Never.                                                                          |
| `_headers`                                      | Security/cache headers for Netlify / Cloudflare Pages. **GitHub Pages ignores it.**                                                | Only if you change hosts.                                                       |
| `.gitignore`                                    | Ignores OS/editor junk, logs, secrets, `*.zip`, CI reports.                                                                        | Rarely.                                                                         |
| `LICENSE`                                       | MIT, unchanged.                                                                                                                    | —                                                                               |
| `.github/workflows/pages-verify.yml`            | CI: required files, truncation guard, JS syntax, broken-link check, image budget, HTML validation, pa11y, Lighthouse.              | Changing CI rules.                                                              |

## Changing the logo

Keep the original master image **outside** the repo (or in `assets/source/`, which is git-ignored). Export `logo-120.webp`, `logo-240.webp`, `logo-512.jpg`, `og-image.jpg` (1200×630), `favicon-32.png`, `apple-touch-icon.png` (180×180) at the same names. The CI warns on any image over 300 KB.

## Consent & analytics

Consent Mode v2 defaults to **denied** for everyone. Choosing Accept stores `tyr_gtag_consent=granted` in `localStorage`; Decline stores `denied`. The footer "Cookie settings" button reopens the banner. The phone link (`data-conversion="tel"`) always sends a Google Ads conversion event and Consent Mode decides what is stored.

## Deep links

Pages are addressable as `/#page-1` … `/#page-6` and `/#all`. They are hash views of one document, so search engines index the single URL `/`.

## CI guard rails

`pages-verify.yml` fails the build if `index.html`/`404.html` has no closing `</html>` (the truncation that broke the previous release), exceeds 150 KB, contains an inline base64 blob over 2 KB, or references a missing file. HTML validation, pa11y and Lighthouse report as warnings and artifacts.

## Release notes — 2026-10-18

- **Repaired:** the committed `index.html` was cut off at 100 KB mid-image (no body, no `</html>`). Rebuilt from the last complete build plus the newer head features.
- **Split:** one 390 KB file became `index.html` (68 KB) + 3 CSS + 5 JS files. The 273 KB inline logo became 3 KB of WebP.
- **Removed:** `Tyr_Website_Publish_Ready.zip` and the 870 KB root JPEG (both duplicated content), the `Accept-CH` meta/header, and the EIN (`taxID`) from the public JSON-LD.
- **Added:** hash routing with working back button, ARIA tabs, keyboard-accessible realm legend, skip link, focus ring, reduced-motion support, Consent Mode default-denied fix, `robots.txt`, `sitemap.xml`, favicon/touch icons, 1200×630 share card, canonical/OG/JSON-LD on `tyrprotocol.pro`.

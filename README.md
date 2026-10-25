# TYR Technical Consulting — Nine Realms Storefront

Static, build-free site for **https://tyrprotocol.pro**, hosted on GitHub Pages (`main` branch, repository root).
Open `index.html` through any static server to preview (`python3 -m http.server 8000`, then visit `http://localhost:8000`).

[![GitHub Pages Verification](https://github.com/KalCola/Tyr-Technical-Consulting-/actions/workflows/pages-verify.yml/badge.svg)](https://github.com/KalCola/Tyr-Technical-Consulting-/actions/workflows/pages-verify.yml)
[![Live Storefront](https://img.shields.io/badge/site-tyrprotocol.pro-blue.svg)](https://tyrprotocol.pro)

---

## File Map & Modular Architecture

| File | What it is | Edit it when… |
| :--- | :--- | :--- |
| `index.html` | Core markup: head/SEO, consent banner, nav, hero deck container, footer. | Changing structural HTML containers or meta tags. |
| `css/base.css` | Design tokens (Nine Realms palette), reset, nav, UA-CH bar, skip link, focus ring. | Changing colors, typography, or global design tokens. |
| `css/components.css` | Hero, tabs, realm legend, cards, bundle banner, workflow, pager, cookie banner. | Adjusting component layouts or styling. |
| `css/responsive.css` | Tablet/mobile/forced-desktop rules (`html[data-device]`), reduced motion. Must load last. | Fixing responsive viewports or device layouts. |
| `js/device-init.js` | Sync initialization: sets `data-device` before first paint (prevents desktop flash on mobile). | Rarely. |
| `js/consent-init.js` | Google Consent Mode v2 defaults (denied), restores saved grants, initializes Ads tag prior to `gtag.js`. | Updating Google Ads / Analytics IDs or consent logic. |
| `js/deck.js` | Hash routing (`#page-N` / `#all`), back button sync, mobile stepper, swipe, keyboard, and `data-action` click handling. | Changing client-side navigation or deck transitions. |
| `js/client-hints.js` | User-Agent Client Hints detection, Auto/Desktop/Mobile override toggles, and telemetry pill. | Modifying device detection or UA-CH parsing. |
| `js/analytics.js` | Consent banner controller, "Cookie settings" trigger, and phone/email conversion event hooks (`AW-18518856067`). | Updating tracking triggers or banner behavior. |
| `js/catalog-loader.js` | Dynamically syncs prices, titles, badges, and specs from `data/catalog.json` into the DOM by `data-sku`. | Rarely (handled via catalog data updates). |
| `data/catalog.json` | Single JSON source of truth for all 15 service SKUs, bundle pricing, 9-Realm hex tokens, and contact telemetry. | Updating pricing, titles, badges, or contact metadata without touching markup. |
| `assets/logo-120.webp`, `logo-240.webp` | Hero emblem assets at 1x / 2x scale (3 KB / 10 KB). | Replacing or updating brand logo graphics. |
| `assets/logo-512.jpg` | High-res logo referenced by JSON-LD structured data. | Updating structured data brand imagery. |
| `assets/og-image.jpg` | 1200×630 OpenGraph social-share preview card. | Updating social preview banners. |
| `assets/favicon-32.png`, `apple-touch-icon.png` | Browser tab and iOS home-screen icons. | Updating favicons. |
| `404.html` | Custom 404 error page (served with HTTP 404, `noindex`). Self-contained absolute URLs. | Rarely. |
| `robots.txt`, `sitemap.xml` | Search crawler directives and canonical sitemap. Update `lastmod` on content releases. | Release updates. |
| `CNAME` | Custom domain configuration for `tyrprotocol.pro`. **Do not delete.** | Never. |
| `.nojekyll` | Directs GitHub Pages to serve files as-is without Jekyll processing. | Never. |
| `_headers` | Security and cache headers for Netlify / Cloudflare Pages. *(Ignored by GitHub Pages).* | Host migration adjustments. |
| `.gitignore` | Excludes OS junk, logs, secrets, archives (`*.zip`), and CI reports. | Rarely. |
| `LICENSE` | MIT License. | — |
| `.github/workflows/pages-verify.yml` | CI pipeline: verifies required files, markup truncation guards, JS syntax, broken links, image budgets, HTML validation, pa11y, and Lighthouse metrics. | Adjusting CI validation rules. |

---

## Architectural Highlights & Modularization

- **Zero-Build Static Delivery**: Pure HTML5, CSS3, and ES Modules running directly from the repository root via GitHub Pages.
- **Payload Optimization**: Monolithic files have been modularized. The previous 390 KB single-file structure is now cleanly separated into a lightweight `index.html` (68 KB), 3 CSS stylesheets, and 5 focused JavaScript modules. Heavy base64 graphics were replaced with optimized 3 KB WebP assets.
- **Client-Side Routing**: Implements hash-based navigation (`/#page-1` through `/#page-6` and `/#all`) with full back-button history support while retaining a single-URL entry point (`/`).
- **Privacy & Consent Compliance**: Google Consent Mode v2 defaults strictly to **denied**. Selections are cached locally (`tyr_gtag_consent`), with a persistent footer control allowing users to modify preferences at any time. Conversions fire cleanly to `AW-18518856067`.

## CI Guard Rails & Verification

The automated GitHub Actions workflow (`pages-verify.yml`) enforces strict quality gates:
1. **Truncation & Integrity**: Fails if `index.html` or `404.html` lacks a closing `</html>` tag or exceeds 150 KB.
2. **Asset Budgets**: Rejects inline base64 blobs exceeding 2 KB and warns on images over 300 KB.
3. **Quality Audits**: Validates markup integrity, checks for broken internal/external links, runs accessibility checks (`pa11y`), and scores performance via Lighthouse.

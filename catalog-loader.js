/* catalog-loader.js
   Quality-of-Life (QoL) JSON synchronizer: fetches data/catalog.json and updates any modified
   service prices, titles, badges, spec tags, or contact details in the DOM without touching index.html. */

(function () {
    async function syncCatalogFromJson() {
        try {
            const res = await fetch("data/catalog.json", { cache: "no-cache" });
            if (!res.ok) return;
            const catalog = await res.json();
            if (!catalog || !catalog.services) return;

            Object.keys(catalog.services).forEach((sku) => {
                const item = catalog.services[sku];
                const card = document.querySelector(`[data-sku="${sku}"]`);
                if (!card) return;

                const titleEl = card.querySelector("h3");
                const priceEl = card.querySelector(".price");
                const priceSubEl = card.querySelector(".price-sub");
                const meaningEl = card.querySelector(".realm-meaning");
                const badgeEl = card.querySelector(".badge");
                const specEl = card.querySelector(".spec-tag");

                if (titleEl && item.title) titleEl.textContent = item.title;
                if (priceEl && item.price) priceEl.textContent = item.price;
                if (priceSubEl && item.priceSub)
                    priceSubEl.textContent = item.priceSub;
                if (meaningEl && item.meaning)
                    meaningEl.textContent = item.meaning;
                if (badgeEl && item.badge) badgeEl.textContent = item.badge;
                if (specEl && item.specTag) specEl.textContent = item.specTag;
            });

            if (catalog.bundles && catalog.bundles["RET-ALL-07"]) {
                const b = catalog.bundles["RET-ALL-07"];
                const bp = document.querySelector(
                    '[data-bundle-price="RET-ALL-07"]',
                );
                if (bp && b.stewardPrice) {
                    bp.innerHTML = `${b.stewardPrice}<span style="font-size: 0.45em; color: var(--text-secondary);">/mo</span>`;
                }
            }
        } catch (err) {
            /* Static HTML fallback already rendered in index.html */
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", syncCatalogFromJson);
    } else {
        syncCatalogFromJson();
    }
})();

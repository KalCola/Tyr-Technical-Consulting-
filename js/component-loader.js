// Component loader
(function () {
  const diag = { components: {}, skus: {}, errors: [] };
  function mountHtml(el, html, path) {
    const tpl = document.createElement("template");
    tpl.innerHTML = html.trim();
    const root = tpl.content.firstElementChild;
    if (!root) return;
    root.setAttribute("data-component", path);
    if (el.classList.contains("active-page")) root.classList.add("active-page");
    else if (el.classList.contains("deck-page")) root.classList.remove("active-page");
    el.replaceWith(root);
  }
  const snap = window.__TYR_FALLBACK_SNAPSHOT__;
  if (snap && snap.components) {
    document.querySelectorAll("[data-component]").forEach((el) => {
      const p = el.getAttribute("data-component");
      if (snap.components[p]) {
        mountHtml(el, snap.components[p], p);
        diag.components[p] = { status: "hydrated-snapshot" };
      }
    });
  }
  async function syncLive() {
    const mounts = Array.from(document.querySelectorAll("[data-component]"));
    await Promise.allSettled(mounts.map(async (el) => {
      const p = el.getAttribute("data-component");
      try {
        const r = await fetch(p, { cache: "no-cache" });
        if (!r.ok) throw new Error("HTTP " + r.status);
        const t = await r.text();
        if (t.trim().length > 20) {
          mountHtml(el, t, p);
          diag.components[p] = { status: "verified-live" };
        }
      } catch (e) {
        diag.errors.push({ key: p, error: String(e) });
      }
    }));
    if (typeof window.syncCatalogFromJson === "function") await window.syncCatalogFromJson();
    if (typeof window.evaluateClientHints === "function") window.evaluateClientHints();
  }
  window.TyrDiag = { log: diag, status: () => diag };
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", syncLive);
  else syncLive();
})();

/* deck.js
   Six-page deck: tabs, hash deep links (#page-N / #all), Dynamic Nine-Realms theme sync,
   mobile one-card stepper (including Page 5 bundle & Page 6 workflow steps), swipe, keyboard, and delegated [data-action] clicks. */

let currentPage = 1;

const pageTitles = {
    1: "Page 1 of 6 (ᛗ Midgard • ᛉ Asgard • ᚱ Vanaheim — Web, Email & QR)",
    2: "Page 2 of 6 (ᚨ Alfheim • ᚷ Nidavellir • ᚲ Muspelheim — Resumes, Contracts & Buying)",
    3: "Page 3 of 6 (ᚠ Svartalfheim • ᛟ Niflheim • ᚦ Jotunheim — Budgets, Vaults & Disputes)",
    4: "Page 4 of 6 (ᛉ Monthly Website, Domain Shield & Vault Caretaker Plans)",
    5: "Page 5 of 6 (ᚠ ᚦ ᚨ Monthly Expense Optimization, Bill Watchdog & Admin Concierge)",
    6: "Page 6 of 6 (ᚱ Bifröst 3-Step Ordering & Zero-Custody Privacy Rule)",
    all: "All 6 Realm Pages Expanded (Full 15-Service Catalog View)",
};

const pageDefaultRealm = {
    1: "midgard",
    2: "alfheim",
    3: "svartalfheim",
    4: "asgard",
    5: "svartalfheim",
    6: "vanaheim",
    all: "midgard",
};

const root = document.documentElement;

function syncActiveRealm(realmName) {
    if (realmName) {
        root.setAttribute("data-active-realm", realmName);
    }
}

function parseHash() {
    if (location.hash === "#all") return "all";
    const m = /^#page-([1-6])$/.exec(location.hash);
    return m ? Number(m[1]) : null;
}

function getMobileStepItems(pageEl) {
    if (!pageEl) return [];
    const cards = Array.from(
        pageEl.querySelectorAll(".grid-3 .card, .workflow-grid .workflow-card"),
    );
    const bundle = pageEl.querySelector(".bundle-banner");
    return bundle ? cards.concat([bundle]) : cards;
}

function showPage(pageId, opts) {
    pageId = pageId === "all" ? "all" : Number(pageId) || 1;
    currentPage = pageId;
    root.setAttribute("data-view", pageId === "all" ? "all" : "paged");

    document.querySelectorAll(".page-tab").forEach((btn) => {
        const on = btn.getAttribute("data-page") === String(pageId);
        btn.classList.toggle("active", on);
        btn.setAttribute("aria-selected", String(on));
    });

    document.querySelectorAll(".deck-page").forEach((page) => {
        const isTarget =
            pageId === "all" ||
            page.getAttribute("data-page-num") === String(pageId);
        page.classList.toggle("active-page", isTarget);
    });

    syncActiveRealm(pageDefaultRealm[pageId] || "midgard");

    if (pageId !== "all") {
        const activePageEl = document.getElementById("page-" + pageId);
        if (activePageEl) {
            const activeBtn =
                activePageEl.querySelector(".mob-step-btn.active") ||
                activePageEl.querySelector(".mob-step-btn");
            if (activeBtn && root.getAttribute("data-device") === "mobile") {
                const btnRealm = activeBtn.getAttribute("data-realm");
                if (btnRealm) syncActiveRealm(btnRealm);
            }
        }
    }

    const statusEl = document.getElementById("pager-status");
    if (statusEl) {
        const isMob = root.getAttribute("data-device") === "mobile";
        statusEl.textContent = isMob ? "" : "Viewing ";
        const strong = document.createElement("strong");
        strong.textContent = isMob
            ? pageId === "all"
                ? "All 6 Pages"
                : `Page ${pageId} of 6`
            : pageTitles[pageId] || "";
        statusEl.appendChild(strong);
    }

    if (opts && opts.push) {
        const hash = pageId === "all" ? "#all" : "#page-" + pageId;
        if (location.hash !== hash) history.pushState(null, "", hash);
    }
}

function stepPage(delta) {
    if (currentPage === "all") {
        showPage(1, { push: true });
        return;
    }
    let next = currentPage + delta;
    if (next < 1) next = 6;
    if (next > 6) next = 1;
    showPage(next, { push: true });
}

function showMobileCard(pageDomId, cardIndex, btnEl) {
    const pageEl = document.getElementById(pageDomId);
    if (!pageEl) return;
    const btns = Array.from(pageEl.querySelectorAll(".mob-step-btn"));
    btns.forEach((b, idx) => {
        const on = btnEl ? b === btnEl : idx === cardIndex;
        b.classList.toggle("active", on);
        b.setAttribute("aria-pressed", String(on));
        if (on && b.getAttribute("data-realm")) {
            syncActiveRealm(b.getAttribute("data-realm"));
        }
    });
    const items = getMobileStepItems(pageEl);
    items.forEach((el, idx) =>
        el.classList.toggle("mob-active-card", idx === cardIndex),
    );
}

function jumpToRealm(pageNum, cardIdx) {
    showPage(pageNum, { push: true });
    const btns = document.querySelectorAll(
        "#page-" + pageNum + " .mob-step-btn",
    );
    if (btns[cardIdx]) {
        showMobileCard("page-" + pageNum, cardIdx, btns[cardIdx]);
        const r = btns[cardIdx].getAttribute("data-realm");
        if (r) syncActiveRealm(r);
    }
}

// Touch swipe support for Mobile 1-Card Stepper
(function initTouchSwipe() {
    let touchStartX = 0;
    let touchStartY = 0;
    document.addEventListener(
        "touchstart",
        (e) => {
            if (!e.touches || !e.touches[0]) return;
            touchStartX = e.touches[0].clientX;
            touchStartY = e.touches[0].clientY;
        },
        { passive: true },
    );

    document.addEventListener(
        "touchend",
        (e) => {
            if (
                document.documentElement.getAttribute("data-device") !==
                    "mobile" ||
                currentPage === "all"
            )
                return;
            if (!e.changedTouches || !e.changedTouches[0]) return;
            const dx = e.changedTouches[0].clientX - touchStartX;
            const dy = e.changedTouches[0].clientY - touchStartY;
            if (Math.abs(dx) < 55 || Math.abs(dx) < Math.abs(dy) * 1.4) return;
            const pageEl = document.getElementById("page-" + currentPage);
            if (!pageEl) return;
            const items = getMobileStepItems(pageEl);
            if (!items.length) return;
            let activeIdx = items.findIndex((c) =>
                c.classList.contains("mob-active-card"),
            );
            if (activeIdx < 0) activeIdx = 0;
            const nextIdx = dx < 0 ? activeIdx + 1 : activeIdx - 1;
            if (nextIdx >= 0 && nextIdx < items.length) {
                const btns = pageEl.querySelectorAll(".mob-step-btn");
                showMobileCard("page-" + currentPage, nextIdx, btns[nextIdx]);
            }
        },
        { passive: true },
    );
})();

// One delegated listener replaces all inline onclick handlers.
document.addEventListener("click", (e) => {
    const el = e.target.closest("[data-action]");
    if (!el) return;
    const d = el.dataset;
    switch (d.action) {
        case "page":
            e.preventDefault();
            showPage(d.page, { push: true });
            break;
        case "step":
            stepPage(Number(d.delta));
            break;
        case "realm":
            jumpToRealm(Number(d.page), Number(d.card));
            break;
        case "card":
            showMobileCard("page-" + d.page, Number(d.card), el);
            break;
        case "device":
            setDeviceMode(d.mode);
            break;
    }
});

document.addEventListener("keydown", (e) => {
    const t = e.target;
    const btn = t.closest && t.closest('[role="button"][data-action]');
    if (btn && (e.key === "Enter" || e.key === " ")) {
        e.preventDefault();
        btn.click();
        return;
    }
    if (e.altKey || e.ctrlKey || e.metaKey || e.shiftKey) return;
    if (
        t &&
        (t.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(t.tagName))
    )
        return;
    if (e.key === "ArrowRight") stepPage(1);
    else if (e.key === "ArrowLeft") stepPage(-1);
});

window.addEventListener("popstate", () => showPage(parseHash() || 1));
showPage(parseHash() || 1);

window.showPage = showPage;
window.stepPage = stepPage;
window.showMobileCard = showMobileCard;
window.jumpToRealm = jumpToRealm;

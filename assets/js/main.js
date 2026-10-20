let currentPage = 1;
let currentDeviceOverride = "auto";
let cachedHighEntropy = null;

const pageTitles = {
    1: "Page 1 of 6 (ᛗ Midgard • ᛉ Asgard • ᚱ Vanaheim — Web, Email & QR)",
    2: "Page 2 of 6 (ᚨ Alfheim • ᚷ Nidavellir • ᚲ Muspelheim — Resumes, Contracts & Buying)",
    3: "Page 3 of 6 (ᚠ Svartalfheim • ᛟ Niflheim • ᚦ Jotunheim — Budgets, Vaults & Disputes)",
    4: "Page 4 of 6 (ᛉ Monthly Website, Domain Shield & Vault Caretaker Plans)",
    5: "Page 5 of 6 (ᚠ ᚦ ᚨ Monthly Expense Optimization, Bill Watchdog & Admin Concierge)",
    6: "Page 6 of 6 (ᚱ Bifröst 3-Step Ordering & Zero-Custody Privacy Rule)",
    all: "All 6 Realm Pages Expanded (Full 15-Service Catalog View)",
};

function showPage(pageId) {
    currentPage = pageId;
    document.documentElement.setAttribute(
        "data-view",
        pageId === "all" ? "all" : "paged",
    );
    document.querySelectorAll(".page-tab").forEach((btn) => {
        btn.classList.toggle(
            "active",
            btn.getAttribute("data-page") == String(pageId),
        );
    });
    document.querySelectorAll(".deck-page").forEach((page) => {
        if (pageId === "all") {
            page.classList.add("active-page");
        } else {
            page.classList.toggle(
                "active-page",
                page.getAttribute("data-page-num") == String(pageId),
            );
        }
    });
    const statusEl = document.getElementById("pager-status");
    if (statusEl) {
        statusEl.innerHTML =
            "Viewing <strong>" + (pageTitles[pageId] || "") + "</strong>";
    }
}

function stepPage(delta) {
    if (currentPage === "all") {
        showPage(1);
        return;
    }
    let next = currentPage + delta;
    if (next < 1) next = 6;
    if (next > 6) next = 1;
    showPage(next);
}

function showMobileCard(pageDomId, cardIndex, btnEl) {
    const pageEl = document.getElementById(pageDomId);
    if (!pageEl) return;
    const btns = pageEl.querySelectorAll(".mob-step-btn");
    btns.forEach((b, idx) =>
        b.classList.toggle("active", btnEl ? b === btnEl : idx === cardIndex),
    );
    const cards = pageEl.querySelectorAll(".grid-3 .card");
    cards.forEach((c, idx) => {
        c.classList.toggle("mob-active-card", idx === cardIndex);
    });
}

function jumpToRealm(pageNum, cardIdx) {
    showPage(pageNum);
    const pageEl = document.getElementById("page-" + pageNum);
    if (pageEl) {
        const btns = pageEl.querySelectorAll(".mob-step-btn");
        if (btns[cardIdx]) {
            showMobileCard("page-" + pageNum, cardIdx, btns[cardIdx]);
        }
    }
}

function isValidHardwareModel(candidate) {
    if (!candidate) return false;
    const cleaned = candidate.replace(/Build\/.*$/i, "").trim();
    // Reject W3C User-Agent Reduction frozen placeholder tokens ('K', 'wv', 'Mobile', single chars)
    if (cleaned.length <= 1) return false;
    if (/^(K|wv|Mobile|Linux|Android|U|en-us|en-gb)$/i.test(cleaned))
        return false;
    return true;
}

function detectPlatformAndModel(uaString, uadPlatform, heValues) {
    const ua = uaString || "";
    const rawModel = heValues && heValues.model ? heValues.model.trim() : "";
    const platVer =
        heValues && heValues.platformVersion
            ? String(heValues.platformVersion).split(".")[0]
            : "";

    if (/Android/i.test(ua) || /Android/i.test(uadPlatform || "")) {
        const uaModelMatch = ua.match(/Android\s+[0-9.]+;\s*([^;)]+)/i);
        const fallbackModel = uaModelMatch
            ? uaModelMatch[1].replace(/Build\/.*$/i, "").trim()
            : "";
        const validModel = isValidHardwareModel(rawModel)
            ? rawModel
            : isValidHardwareModel(fallbackModel)
              ? fallbackModel
              : "";
        if (validModel) return `Android (${validModel})`;
        if (platVer && Number(platVer) > 0) return `Android ${platVer}`;
        return "Android";
    }
    if (/iPhone|iPad|iPod/i.test(ua) || /iOS/i.test(uadPlatform || "")) {
        return /iPad/i.test(ua) ? "iPadOS" : "iOS";
    }
    // Detect mobile browser 'Request Desktop Site' spoofing (Linux x86_64 or MacIntel on touch handset)
    const isCoarseTouch =
        navigator.maxTouchPoints &&
        navigator.maxTouchPoints >= 2 &&
        window.matchMedia &&
        window.matchMedia("(any-pointer: coarse)").matches;
    if (isCoarseTouch && /X11;\s*Linux\s*x86_64/i.test(ua)) {
        return "Android (Desktop Site)";
    }
    if (
        isCoarseTouch &&
        /Macintosh/i.test(ua) &&
        navigator.maxTouchPoints >= 4
    ) {
        return "iOS/iPadOS";
    }
    if (/Windows/i.test(uadPlatform || "") || /Windows NT/i.test(ua))
        return "Windows";
    if (/Mac/i.test(uadPlatform || "") || /Macintosh|Mac OS X/i.test(ua))
        return "macOS";
    if (/CrOS/i.test(ua) || /Chrome OS/i.test(uadPlatform || ""))
        return "ChromeOS";
    if (/Linux/i.test(uadPlatform || "") || /Linux/i.test(ua)) return "Linux";
    return uadPlatform || "Desktop OS";
}

// Standards-Compliant User-Agent Client Hints (UA-CH) Detection & Screen Dynamics Engine
async function evaluateClientHints() {
    const ua = navigator.userAgent || "";
    let isMobileCH = false;
    let rawUadPlatform = "";

    if (navigator.userAgentData) {
        isMobileCH = Boolean(navigator.userAgentData.mobile);
        rawUadPlatform = navigator.userAgentData.platform || "";
        if (
            !cachedHighEntropy &&
            typeof navigator.userAgentData.getHighEntropyValues === "function"
        ) {
            try {
                cachedHighEntropy =
                    await navigator.userAgentData.getHighEntropyValues([
                        "platform",
                        "platformVersion",
                        "model",
                        "formFactors",
                    ]);
            } catch (err) {
                cachedHighEntropy = {};
            }
        }
        if (cachedHighEntropy) {
            if (cachedHighEntropy.platform)
                rawUadPlatform = cachedHighEntropy.platform;
            if (
                Array.isArray(cachedHighEntropy.formFactors) &&
                cachedHighEntropy.formFactors.includes("Mobile")
            ) {
                isMobileCH = true;
            }
        }
    } else {
        isMobileCH = /Android|iPhone|iPad|iPod|Mobile/i.test(ua);
    }

    if (/Android|iPhone|iPad|iPod|Mobile/i.test(ua)) {
        isMobileCH = true;
    }

    const platformLabel = detectPlatformAndModel(
        ua,
        rawUadPlatform,
        cachedHighEntropy,
    );
    const vpMeta = document.getElementById("viewport-meta");

    // Reset any prior iframe zoom before measuring
    document.body.style.zoom = "";
    document.body.style.width = "";

    const physicalScreenNarrow =
        (window.screen &&
            window.screen.width > 0 &&
            window.screen.width <= 860) ||
        window.innerWidth <= 860;

    // Handle Viewport Meta & iframe proportional zoom when user forces Desktop on a narrow screen
    if (currentDeviceOverride === "desktop") {
        if (vpMeta) {
            vpMeta.setAttribute(
                "content",
                "width=1240, initial-scale=0.33, minimum-scale=0.25, maximum-scale=5.0",
            );
        }
        document.documentElement.setAttribute(
            "data-forced-desktop",
            physicalScreenNarrow ? "true" : "false",
        );
        const actualClientW =
            document.documentElement.clientWidth || window.innerWidth;
        if (actualClientW > 0 && actualClientW < 1140) {
            const targetDesktopW = 1220;
            const scaleRatio = actualClientW / targetDesktopW;
            document.body.style.width = targetDesktopW + "px";
            document.body.style.zoom = String(scaleRatio);
        }
    } else {
        if (vpMeta) {
            vpMeta.setAttribute(
                "content",
                "width=device-width, initial-scale=1.0, maximum-scale=5.0",
            );
        }
        document.documentElement.setAttribute("data-forced-desktop", "false");
    }

    const viewportMobile =
        window.innerWidth <= 860 ||
        window.matchMedia("(pointer: coarse) and (max-width: 920px)").matches;
    const autoDetectedMobile = isMobileCH || viewportMobile;
    const effectiveMobile =
        currentDeviceOverride === "mobile"
            ? true
            : currentDeviceOverride === "desktop"
              ? false
              : autoDetectedMobile;

    document.documentElement.setAttribute(
        "data-device",
        effectiveMobile ? "mobile" : "desktop",
    );
    document.documentElement.setAttribute("data-uach", currentDeviceOverride);

    const telemetryEl = document.getElementById("uach-telemetry");
    if (telemetryEl) {
        const chToken = autoDetectedMobile ? "?1" : "?0";
        const modeDesc = effectiveMobile
            ? currentDeviceOverride === "mobile" && !autoDetectedMobile
                ? "Mobile Override"
                : "Mobile"
            : currentDeviceOverride === "desktop" && autoDetectedMobile
              ? "Desktop Override"
              : "Desktop";
        const fullText = effectiveMobile
            ? `UA-CH: ${chToken} (${modeDesc}) • ${platformLabel} • 1-Card Stepper`
            : `UA-CH: ${chToken} (${modeDesc}) • ${platformLabel} • 3-Col Deck`;
        telemetryEl.setAttribute("title", fullText);
        const unzoomedW = window.innerWidth;
        if (effectiveMobile) {
            const compactPlat = platformLabel.replace(
                /^Android\s*\(([^)]+)\)$/i,
                "$1",
            );
            telemetryEl.textContent =
                unzoomedW <= 390
                    ? `UA-CH:${chToken} • ${compactPlat}`
                    : `UA-CH: ${chToken} • ${compactPlat}`;
        } else {
            telemetryEl.textContent = fullText;
        }
    }
}

function setDeviceMode(mode) {
    currentDeviceOverride = mode;
    ["auto", "desktop", "mobile"].forEach((m) => {
        const el = document.getElementById("btn-uach-" + m);
        if (el) el.classList.toggle("active", m === mode);
    });
    evaluateClientHints();
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
            const cards = Array.from(pageEl.querySelectorAll(".grid-3 .card"));
            if (!cards.length) return;
            let activeIdx = cards.findIndex((c) =>
                c.classList.contains("mob-active-card"),
            );
            if (activeIdx < 0) activeIdx = 0;
            const nextIdx = dx < 0 ? activeIdx + 1 : activeIdx - 1;
            if (nextIdx >= 0 && nextIdx < cards.length) {
                const btns = pageEl.querySelectorAll(".mob-step-btn");
                showMobileCard("page-" + currentPage, nextIdx, btns[nextIdx]);
            }
        },
        { passive: true },
    );
})();

window.addEventListener("resize", evaluateClientHints);
window.addEventListener("keydown", (e) => {
    if (e.key === "ArrowRight") stepPage(1);
    if (e.key === "ArrowLeft") stepPage(-1);
});
evaluateClientHints();

/* client-hints.js
   User-Agent Client Hints detection, Auto/Desktop/Mobile override, telemetry pill. Defers; exposes setDeviceMode(). */

let currentDeviceOverride = "auto";
let cachedHighEntropy = null;

function isValidHardwareModel(candidate) {
    if (!candidate) return false;
    const cleaned = candidate.replace(/Build\/.*$/i, "").trim();
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
    const physicalScreenNarrow =
        (window.screen &&
            window.screen.width > 0 &&
            window.screen.width <= 860) ||
        window.innerWidth <= 860;

    // Never double-scale with document.body.style.zoom; keep 100vw fluid grid intact
    document.body.style.zoom = "";
    document.body.style.width = "";
    if (vpMeta) {
        vpMeta.setAttribute(
            "content",
            "width=device-width, initial-scale=1.0, maximum-scale=5.0",
        );
    }

    if (currentDeviceOverride === "desktop") {
        document.documentElement.setAttribute(
            "data-forced-desktop",
            physicalScreenNarrow ? "true" : "false",
        );
    } else {
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
            ? `UA-CH: ${chToken} (${modeDesc}) • ${platformLabel} • 1-Card Zero-Scroll`
            : `UA-CH: ${chToken} (${modeDesc}) • ${platformLabel} • 3-Col Zero-Scroll Deck`;
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
        if (el) {
            el.classList.toggle("active", m === mode);
            el.setAttribute("aria-pressed", String(m === mode));
        }
    });
    evaluateClientHints();
}

let resizeTimer = null;
window.addEventListener("resize", () => {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(evaluateClientHints, 120);
});
evaluateClientHints();

window.setDeviceMode = setDeviceMode;
window.evaluateClientHints = evaluateClientHints;

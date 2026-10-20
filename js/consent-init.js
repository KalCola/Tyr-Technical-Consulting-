/* consent-init.js
   Google Consent Mode v2. Runs synchronously BEFORE gtag.js: default = denied for everyone, then restores a stored "granted" choice. */

window.dataLayer = window.dataLayer || [];
window.__pendingGtagEvents = window.__pendingGtagEvents || [];
function gtag() {
    window.dataLayer.push(arguments);
}
window.gtag = gtag;

try {
    window._tyr_gtag_consent =
        window.localStorage &&
        window.localStorage.getItem("tyr_gtag_consent") === "granted";
} catch (e) {
    window._tyr_gtag_consent = false;
}

gtag("consent", "default", {
    ad_storage: window._tyr_gtag_consent ? "granted" : "denied",
    analytics_storage: window._tyr_gtag_consent ? "granted" : "denied",
    ad_user_data: window._tyr_gtag_consent ? "granted" : "denied",
    ad_personalization: window._tyr_gtag_consent ? "granted" : "denied",
    wait_for_update: 500,
});

try {
    if (window._tyr_gtag_consent) {
        gtag("consent", "update", {
            ad_storage: "granted",
            analytics_storage: "granted",
            ad_user_data: "granted",
            ad_personalization: "granted",
        });
    }
} catch (e) {
    /* storage blocked: stay denied */
}

gtag("js", new Date());
gtag("config", "AW-18518856067");

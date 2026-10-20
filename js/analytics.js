/* analytics.js
   Consent banner wiring + Google Ads phone & email conversion tracking (AW-18518856067). */

(function () {
    var KEY = "tyr_gtag_consent";
    var banner = document.getElementById("cookie-consent-banner");
    var SEND_TO = "AW-18518856067/dUg4CNiWnKAdEIOjvf5E";

    function read() {
        try {
            return window.localStorage.getItem(KEY);
        } catch (e) {
            return null;
        }
    }
    function save(v) {
        try {
            window.localStorage.setItem(KEY, v);
        } catch (e) {}
    }
    function choose(v) {
        window._tyr_gtag_consent = v === "granted";
        save(v);
        gtag("consent", "update", {
            ad_storage: v,
            analytics_storage: v,
            ad_user_data: v,
            ad_personalization: v,
        });
        if (banner) banner.hidden = true;
    }
    function openBanner() {
        if (!banner) return;
        banner.hidden = false;
        var b = document.getElementById("cookie-consent-decline");
        if (b) b.focus();
    }

    window._tyr_grantConsent = function () {
        choose("granted");
    };
    window._tyr_denyConsent = function () {
        choose("denied");
    };
    window._tyr_openConsentBanner = openBanner;

    window.gtag_report_conversion = function (url) {
        var done = false;
        function go() {
            if (!done) {
                done = true;
                if (url) window.location.href = url;
            }
        }
        try {
            gtag("event", "conversion", {
                send_to: SEND_TO,
                value: 1.0,
                currency: "USD",
                event_callback: go,
            });
        } catch (err) {
            go();
        }
        setTimeout(go, 350);
        return false;
    };

    var accept = document.getElementById("cookie-consent-accept");
    var decline = document.getElementById("cookie-consent-decline");
    if (accept)
        accept.addEventListener("click", function () {
            choose("granted");
        });
    if (decline)
        decline.addEventListener("click", function () {
            choose("denied");
        });
    if (read() === null && banner) banner.hidden = false;

    document.addEventListener("click", function (e) {
        var open = e.target.closest('[data-action="consent-open"]');
        if (open) {
            openBanner();
            return;
        }
        var link = e.target.closest("[data-conversion]");
        if (!link) return;
        var href = link.getAttribute("href");
        if (!href) return;
        e.preventDefault();
        window.gtag_report_conversion(href);
    });
})();

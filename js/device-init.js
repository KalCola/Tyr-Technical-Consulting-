/* device-init.js
   Synchronous, tiny: sets html[data-device] and html[data-active-realm] before first paint so phones never flash the desktop layout. */

(function () {
    var ua = navigator.userAgent || "";
    var uadMob = navigator.userAgentData
        ? Boolean(navigator.userAgentData.mobile)
        : false;
    var coarse =
        window.matchMedia &&
        window.matchMedia("(pointer: coarse) and (max-width: 920px)").matches;
    var isMob =
        uadMob ||
        /Android|iPhone|iPad|iPod|Mobile/i.test(ua) ||
        window.innerWidth <= 860 ||
        coarse;
    document.documentElement.setAttribute(
        "data-device",
        isMob ? "mobile" : "desktop",
    );
    if (!document.documentElement.getAttribute("data-active-realm")) {
        document.documentElement.setAttribute("data-active-realm", "midgard");
    }
})();

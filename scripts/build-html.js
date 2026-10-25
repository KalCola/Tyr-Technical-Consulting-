const fs = require("fs");
const html = fs.readFileSync("_backup/index.html", "utf8");

function extract(re, tag, path) {
  const m = re.exec(html);
  const open = new RegExp("<" + tag + "(\\s|>|/)", "gi");
  const close = new RegExp("</" + tag + ">", "gi");
  let d = 0, p = m.index;
  while (p < html.length) {
    open.lastIndex = close.lastIndex = p;
    const o = open.exec(html), c = close.exec(html);
    if (o && o.index < c.index) {
      d++;
      p = o.index + o[0].length;
    } else {
      d--;
      p = c.index + c[0].length;
      if (!d) {
        return html.slice(m.index, p).replace(new RegExp("^<" + tag), "<" + tag + ' data-component="' + path + '"');
      }
    }
  }
}

const specs = [
  ["components/cookie-banner.html", /<div[^>]*id="cookie-consent-banner"[^>]*>/, "div"],
  ["components/top-nav.html", /<nav[^>]*class="top-nav"[^>]*>/, "nav"],
  ["components/hero.html", /<header[^>]*class="hero"[^>]*>/, "header"],
  ["components/deck-nav.html", /<div[^>]*class="deck-nav-panel"[^>]*>/, "div"],
  ["components/page-1.html", /<div[^>]*id="page-1"[^>]*>/, "div"],
  ["components/page-2.html", /<div[^>]*id="page-2"[^>]*>/, "div"],
  ["components/page-3.html", /<div[^>]*id="page-3"[^>]*>/, "div"],
  ["components/page-4.html", /<div[^>]*id="page-4"[^>]*>/, "div"],
  ["components/page-5.html", /<div[^>]*id="page-5"[^>]*>/, "div"],
  ["components/page-6.html", /<div[^>]*id="page-6"[^>]*>/, "div"],
  ["components/bottom-dock.html", /<div[^>]*class="bottom-dock-wrap"[^>]*>/, "div"],
];

fs.mkdirSync("components", { recursive: true });
const components = {};
for (const [p, r, t] of specs) {
  const out = extract(r, t, p);
  fs.writeFileSync(p, out, "utf8");
  components[p] = out;
}
fs.writeFileSync(
  "js/fallback-snapshot.js",
  "window.__TYR_FALLBACK_SNAPSHOT__ = " + JSON.stringify({ components }) + ";\n",
  "utf8"
);
if (fs.existsSync("index.html")) {
  const idx = fs.readFileSync("index.html", "utf8").replace(/src="(js\/[^"?]+)\.js"/g, 'src="$1.js?v=5"');
  fs.writeFileSync("index.html", idx, "utf8");
}
fs.rmSync("_backup", { recursive: true, force: true });
console.log("Rebuilt", Object.keys(components).length, "components");

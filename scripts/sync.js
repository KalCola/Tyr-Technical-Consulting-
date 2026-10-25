const fs = require("fs");
const L = fs.readFileSync("_backup/index.html", "utf8").split(/\r?\n/);
const C = {};
const S = [
  ["cookie-banner", 109, 116], ["top-nav", 118, 141], ["hero", 145, 177],
  ["deck-nav", 179, 232], ["page-1", 236, 380], ["page-2", 384, 527],
  ["page-3", 531, 675], ["page-4", 679, 820], ["page-5", 825, 1009],
  ["page-6", 1013, 1066], ["bottom-dock", 1068, 1083]
];
for (const [k, s, e] of S) {
  const p = "components/" + k + ".html";
  const h = L.slice(s, e).join("\n").trim().replace(/^(<\w+)/, '$1 data-component="' + p + '"');
  fs.writeFileSync(p, h + "\n");
  C[p] = h + "\n";
}
fs.writeFileSync("js/fallback-snapshot.js", "window.__TYR_FALLBACK_SNAPSHOT__ = " + JSON.stringify({ components: C }) + ";\n");
fs.rmSync("_backup", { recursive: true, force: true });
console.log("Synced 11 components");

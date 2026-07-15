# GitHub Pages landing page

Constraints: single static page, strict black and white, globe visual, zero
external libraries or assets, served from the `docs/` folder on `main`
(enable in repo settings or via the Pages API).

## Layout

One `index.html` with inline `<style>` and minimal inline JS only if the
canvas option is chosen. Sections:

1. Hero: app name, one-liner, the globe.
2. Features: 100+ free profiles, best-server auto-connect, AdGuard DNS,
   open source, no accounts, no trackers.
3. Download button linking to the latest GitHub Release APK plus checksums.
4. Footer: source link, GPL-2.0-or-later notice, disclaimer that servers
   are third-party VPN Gate volunteers.

## Globe techniques with zero dependencies

1. **Inline SVG globe (recommended)**: `<circle>` outline plus several
   `<ellipse>` meridians/parallels (`stroke: #fff; fill: none`, `transform`
   for the squish). CSS `@keyframes` rotation on the meridian group. Sharp
   at any DPI, trivially monochrome, no JS.
2. **Pure CSS wireframe globe**: `border-radius: 50%` circle with nested
   elliptical divs and `rotateX`/`scaleX` transforms for graticule lines,
   animated with keyframes.
3. **Canvas dot globe**: ~50 lines of vanilla JS projecting lat/long points
   orthographically with a rotating angle, white dots on black. More
   "techy", still self-contained.

Honor `prefers-reduced-motion` by disabling the spin. Keep the palette
literally `#000` and `#fff` with at most one gray for muted text. Keep all
assets inline so the page works offline.

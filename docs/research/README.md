# Aperture VPN Research

Research for a free, open-source Android VPN client that aggregates 100+ free
OpenVPN profiles, auto-connects to the best server, and forces AdGuard DNS.
Produced by a kestrel fan-out (cursor, kimi, opus agents; run
`20260716-013447`) and verified against live endpoints on 2026-07-16.

## Documents

1. [OpenVPN profile sources and package downloads](01-openvpn-profile-sources.md)
2. [Android OpenVPN engine options](02-android-openvpn-engines.md)
3. [Best-server selection](03-best-server-selection.md)
4. [AdGuard DNS enforcement](04-adguard-dns.md)
5. [App architecture and distribution](05-architecture-and-distribution.md)
6. [GitHub Pages landing page](06-landing-page.md)

## Recommended stack

- **Profiles:** VPN Gate public API `https://www.vpngate.net/api/iphone/`
  (CSV, 15 columns, base64 column decodes to a complete inline-cert `.ovpn`).
  Live-verified 2026-07-16: 97 server rows. Accumulate profiles across
  refreshes in a local cache to guarantee 100+.
- **Engine:** ics-openvpn (`de.blinkt.openvpn`,
  `https://github.com/schwabe/ics-openvpn`) vendored as a git submodule.
  GPLv2, so Aperture is licensed GPL-2.0-or-later with full public source.
- **Best server:** TCP connect-time probing (no ICMP on unrooted Android),
  bounded coroutine concurrency, weighted score combining live latency with
  VPN Gate metadata, automatic fallback down the ranked list.
- **DNS:** AdGuard `94.140.14.14` / `94.140.15.15` injected as
  `dhcp-option DNS`, server-pushed DNS dropped with
  `pull-filter ignore "dhcp-option DNS"`, plus ics-openvpn profile-level
  DNS override.
- **App:** Kotlin, Jetpack Compose, coroutines, minimal dependencies.
  Strict black-and-white UI with a globe motif.
- **Distribution:** signed APK on GitHub Releases via GitHub Actions,
  optional F-Droid later. Landing page on GitHub Pages, pure `#000`/`#fff`,
  inline SVG globe, zero external assets.

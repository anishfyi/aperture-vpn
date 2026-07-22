<p align="center">
  <img src="docs/logo.svg" alt="APRTR spartan helmet" width="88">
  <br><br>
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/aprtr-wordmark-dark.svg">
    <img alt="APRTR" src="docs/aprtr-wordmark-light.svg" width="260">
  </picture>
</p>

Free, open-source Android VPN that aggregates 100+ free OpenVPN profiles from
[VPN Gate](https://www.vpngate.net/), probes and ranks servers, connects to the
best one with one tap via embedded [ics-openvpn](https://github.com/schwabe/ics-openvpn),
and forces [AdGuard DNS](https://adguard-dns.io/) for ad and tracker blocking.
Strict black-and-white UI with a spartan helmet mark.

- **Download:** [Latest release APK](https://github.com/anishfyi/aperture-vpn/releases/latest)
- **Landing page:** [GitHub Pages](https://anishfyi.github.io/aperture-vpn/)
- **License:** GPL-2.0-or-later

## Features

- 100+ free OpenVPN profiles (VPN Gate API, accumulated across refreshes)
- TCP connect-time probing and weighted server scoring
- Smart connect with automatic fallback through the top-ranked servers
- AdGuard DNS (`94.140.14.14` / `94.140.15.15`) injected into every profile
- No accounts, no analytics, no trackers
- Monochrome Compose UI

## How it works

1. **Profiles:** `VpnGateFetcher` downloads the VPN Gate CSV API, parses the
   15-column format (comma-safe split), and merges results into a persistent
   JSON cache keyed by IP so the pool stays above 100 servers across refreshes.
2. **DNS:** `OvpnParser` base64-decodes each profile and injects AdGuard
   `dhcp-option` lines plus `pull-filter ignore "dhcp-option DNS"`.
3. **Selection:** `Prober` runs bounded-concurrency TCP connect timing;
   `Scorer` ranks servers with the weighted formula from
   `docs/research/03-best-server-selection.md`.
4. **Tunnel:** `ConnectionManager` drives ics-openvpn (`ConfigParser`,
   `VpnProfile`, `ProfileManager`, `VPNLaunchHelper`, `VpnStatus`) with DNS
   override flags set on the profile.

## Build

Requires JDK 17, Android SDK/NDK (CI uses GitHub Actions).

```bash
git clone https://github.com/anishfyi/aperture-vpn.git
cd aperture-vpn
git submodule update --init --recursive
git -C vendor/ics-openvpn apply ../../patches/ics-openvpn-embedded.patch
gradle :app:assembleSkeletonOvpn23Debug
```

CI builds and verifies the debug APK on every push/PR to `main`. Signed release
APKs are attached to GitHub Releases on `v*` tags.

## Legal disclaimer

VPN Gate servers are operated by third-party volunteers with varying logging
policies. Aperture VPN aggregates public VPN Gate data and does not operate
infrastructure. No uptime or privacy guarantee is provided. Use at your own risk.

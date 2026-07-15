# App architecture and distribution

## Stack

Kotlin, Jetpack Compose (fits the custom monochrome design), coroutines +
Flow, DataStore for settings, a JSON file for the profile cache (leaner than
Room). Dependencies kept minimal: ics-openvpn (vendored), OkHttp or plain
`HttpURLConnection`, Compose, Lifecycle/ViewModel. No analytics, no ads, no
trackers.

## Module flow

```
ProfileFetcher -> OvpnParser -> Prober -> Scorer/ServerSelector -> ConnectionManager -> Compose UI
```

- **ProfileFetcher**: GET the VPN Gate CSV, parse the `*`/`#` framed format,
  merge into a persistent cache keyed by IP (union across refreshes so the
  pool stays at 100+), TTL 6-24h, retry with backoff.
- **OvpnParser**: base64-decode `OpenVPN_ConfigData_Base64`, extract
  `remote`/`proto`/`port`, inject AdGuard DNS + pull-filter, produce a
  normalized `ServerProfile` model.
- **Prober**: bounded-concurrency TCP connect timing (Semaphore(64),
  2s timeout).
- **Scorer/ServerSelector**: weighted score, ranked list, best pick,
  automatic fallback.
- **ConnectionManager**: thin wrapper over ics-openvpn (`ConfigParser`,
  `VpnProfile`, `ProfileManager`, `OpenVPNService`, `VpnStatus` listeners).
  Owns the consent flow, foreground notification, DNS override flags,
  connect/disconnect, tunnel verification.
- **UI**: single-activity Compose. Screens: Home (globe + one-tap connect +
  status), server list with country filter, settings. State via ViewModel +
  StateFlow.

## Shipping without Google Play

- **GitHub Releases APK** as the primary channel, triggered on `v*` tags.
- **GitHub Actions workflow**: checkout with submodules, set up JDK +
  Android SDK/NDK, `./gradlew assembleRelease`, sign, attach to release.
- **Signing in CI**: keystore stored as a base64 GitHub Actions secret,
  decoded at build time; pass store/key passwords via secrets into a
  `signingConfig` or run `apksigner` after the build
  (`r0adkll/sign-android-release` or manual). Never commit the keystore.
  Publish a SHA-256 `checksums.txt` next to the APK.
  Useful actions: `actions/setup-java`, `r0adkll/sign-android-release`,
  `softprops/action-gh-release`.
- **F-Droid later**: requires fully FOSS deps (satisfied) and a
  reproducible build recipe in `fdroiddata`; other OpenVPN clients are on
  F-Droid so the native builds are known to work there. Do this after the
  GitHub channel is stable.
- **License**: GPL-2.0-or-later for the whole repo (forced by embedding
  ics-openvpn, and desirable anyway).

# Android OpenVPN engine options

## ics-openvpn (recommended)

- Source: `https://github.com/schwabe/ics-openvpn`, package
  `de.blinkt.openvpn`. The de-facto open-source OpenVPN client for Android.
- License: GPL-2.0-or-later (with the OpenVPN/OpenSSL linking exception).
  Embedding it makes the whole APK a GPLv2 combined work, so Aperture is
  GPL-2.0-or-later with full source published. Fine for this project.

### Integration mode A: embedded library module (chosen)

- Vendor the repo as a git submodule and include its reusable Gradle module
  as a subproject (`implementation project(":openvpn")` pointing at the
  submodule's `main` module directory).
- There is no reliably maintained first-party Maven artifact; JitPack builds
  of `com.github.schwabe:ics-openvpn` have historically been fragile because
  the project ships native `.so` builds and submodules. Plan on the
  submodule.
- Drive it programmatically: parse config with `ConfigParser`, build a
  `VpnProfile`, register it via `ProfileManager`, start `OpenVPNService`
  (via `VPNLaunchHelper`), observe state through `VpnStatus` listeners.
- Cost: NDK build of openvpn/openssl native code, medium effort, but full
  one-tap control inside a single APK.

### Integration mode B: remote AIDL API (rejected for v1)

- The separately installed "OpenVPN for Android" app exposes
  `de.blinkt.openvpn.api.IOpenVPNAPIService` with `startVPN(inlineConfig)`,
  status callback, and `disconnect()`.
- Low integration effort and it decouples licensing, but the user must
  install and authorize a second app, which defeats the one-tap single-APK
  goal. Keep only as a possible fallback.

## OpenVPN 3 Core (rejected)

- Source: `https://github.com/OpenVPN/openvpn3`, C++ core used by OpenVPN
  Connect. Current license AGPL-3.0.
- No official Android AAR; requires your own JNI bridge, NDK build, and
  VpnService plumbing. Significantly more effort with heavier copyleft.
  Skip unless Aperture outgrows ics-openvpn.

## Not applicable

- OpenVPN Connect: closed source, not embeddable.
- WireGuard (`com.wireguard.android:tunnel`): clean maintained AAR, but the
  free profile sources are OpenVPN-only. Future direction at most.

## Android VpnService requirements (any engine)

- Manifest: service with `android:permission="android.permission.BIND_VPN_SERVICE"`
  and intent filter `android.net.VpnService` (ics-openvpn declares this).
- User consent: `VpnService.prepare(context)`; if non-null, launch the
  returned Intent for result to show the mandatory system consent dialog.
  The tunnel can only start after `RESULT_OK`.
- Foreground service with an ongoing notification. Android 13+ needs
  `POST_NOTIFICATIONS`; Android 14+ needs a declared
  `foregroundServiceType` plus the matching permission.
- Only one active VPN per device; handle `onRevoke` and always-on VPN.
- Baseline: minSdk 24, targetSdk latest. Verify the minSdk of the exact
  ics-openvpn tag vendored (recent tags have raised it).

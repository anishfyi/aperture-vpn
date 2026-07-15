# AdGuard DNS enforcement

Target resolvers (AdGuard "Default" ad and tracker blocking profile):

- IPv4: `94.140.14.14`, `94.140.15.15`
- IPv6: `2a10:50c0::ad1:ff`, `2a10:50c0::ad2:ff`
- Reference: `https://adguard-dns.io/en/public-dns.html` (re-verify IPs
  before release in case they change)

Goal: every DNS query goes to AdGuard regardless of what the VPN server
pushes.

## Config-level injection

Rewrite every parsed `.ovpn` before handing it to the engine:

```
dhcp-option DNS 94.140.14.14
dhcp-option DNS 94.140.15.15
pull-filter ignore "dhcp-option DNS"
```

The `pull-filter` line drops server-pushed DNS so the injected AdGuard
resolvers always win regardless of push order.

## ics-openvpn profile-level override (belt and suspenders)

`VpnProfile` exposes exactly the fields needed; set them in addition to the
config directives so behavior is deterministic:

- `mOverrideDNS = true` ("Override DNS settings by server")
- `mDNS1 = "94.140.14.14"`, `mDNS2 = "94.140.15.15"`

ics-openvpn passes these to `VpnService.Builder.addDnsServer()` when it
builds the tun interface.

## Leak considerations

- Capture the default route (`redirect-gateway def1`) so DNS packets to
  AdGuard actually travel inside the tunnel instead of egressing in the
  clear.
- IPv6: if the device has IPv6 and the tunnel is IPv4-only, IPv6 DNS and
  traffic leak around it. Either add the AdGuard IPv6 resolvers and route
  IPv6, or block IPv6 inside the tunnel.
- Android Private DNS (DoT) set to a specific hostname can bypass tunnel
  DNS on some OEM/version combinations. Detect and warn the user.
- Apps with built-in DNS-over-HTTPS (some browsers) bypass system DNS
  entirely; document this limitation, it cannot be fixed without DPI.
- Ship an in-app DNS leak test (resolve a canary domain through the tunnel
  and compare) so users can verify.

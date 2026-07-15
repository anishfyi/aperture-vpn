# OpenVPN profile sources and package downloads

Goal: at least 100 free `.ovpn` profiles fetched programmatically, no accounts.

## VPN Gate public API (primary source)

- Endpoint: `https://www.vpngate.net/api/iphone/` (plain `GET`, returns CSV;
  the `/iphone/` path is the generic machine-readable list, not iOS-specific).
- Operator: SoftEther project, University of Tsukuba. Academic research
  service run on volunteer servers.
- Live check (2026-07-16): 97 data rows, ~2 MB body, 15-column header
  confirmed, and the base64 column of the first row decoded to a valid
  `.ovpn` (`dev tun`, `proto tcp`, `remote 219.100.37.3 443`,
  `cipher AES-128-CBC`). Counts fluctuate between roughly 70 and 150, so a
  single snapshot does not guarantee 100+; the app must accumulate a union
  of profiles across refreshes (dedupe by IP) and only then prune stale ones.

### CSV shape

```
*vpn_servers
#HostName,IP,Score,Ping,Speed,CountryLong,CountryShort,NumVpnSessions,Uptime,TotalUsers,TotalTraffic,LogType,Operator,Message,OpenVPN_ConfigData_Base64
<row>
...
*
```

Parsing rules:

- Skip lines starting with `*` (frame markers) and strip the leading `#`
  from the header line.
- Split each data row into exactly 15 fields; the final field
  (`OpenVPN_ConfigData_Base64`) is a long comma-free base64 blob, so split
  with a limit rather than naively on every comma (the free-text `Message`
  field can contain commas).
- Skip rows with an empty base64 field.

### Columns that matter

| Column | Meaning |
|--------|---------|
| `IP` | Server public IPv4, use as the dedupe key |
| `Score` | VPN Gate composite quality score, higher is better |
| `Ping` | RTT in ms measured from VPN Gate's infra, can be `-` |
| `Speed` | Throughput in bits per second |
| `CountryLong` / `CountryShort` | Country name and 2-letter code |
| `NumVpnSessions` | Current sessions, a proxy for load |
| `LogType` | Operator logging policy string (e.g. `2weeks`) |
| `OpenVPN_ConfigData_Base64` | Base64 of the complete `.ovpn` file |

### Decoding the package

`base64 decode` of the last column yields a full UTF-8 `.ovpn` with the
`remote <ip> <port>` line, `proto`, `dev tun`, cipher settings, and inline
`<ca>`, `<cert>`, `<key>` blocks. No credentials and no extra assembly
needed; feed it straight to the OpenVPN engine after DNS injection.

### Rate limits and terms

- No published hard limit, but the project asks clients not to poll
  aggressively. Fetch a few times per day at most, cache with a 6-24h TTL,
  send a real `User-Agent`, back off on failure.
- Servers are third-party volunteers with varying logging policies
  (`LogType`); the app must surface this. No uptime guarantee.
- Current terms live at `https://www.vpngate.net/en/notice.aspx`.
- VPN Gate is blocked or restricted in some jurisdictions; the app
  aggregates public data and should not hardcode circumvention behavior.
- Mirrors rotate and are unreliable (a tested mirror returned 0 rows);
  treat mirrors as optional fallbacks behind config, prefer the primary
  endpoint plus local cache.

## Secondary sources (optional, not v1)

| Source | URL | Programmatic? | Caveats |
|--------|-----|---------------|---------|
| freeopenvpn.org | `https://www.freeopenvpn.org/` | No API | Daily-rotating username/password shown on the page, needs scraping, uses `auth-user-pass`, fragile and ToS-unclear |
| VPNBook | `https://www.vpnbook.com/freevpn` | No API | ZIP bundles, weekly-rotating password posted on the page, scraping-dependent |
| GitHub `.ovpn` collections | various | Yes | Mostly re-scraped VPN Gate data with poor provenance and freshness, not recommended |
| Proton VPN Free / Windscribe | vendor sites | No | Account-gated, no public unauthenticated server list, out of scope |

**Conclusion:** VPN Gate is the only legitimate, unauthenticated,
programmatic source that can deliver 100+ live profiles. Ship v1 on VPN Gate
only, with the accumulate-across-refreshes cache to hold the 100+ floor, and
keep secondary scrapers behind a feature flag with in-app disclaimers.

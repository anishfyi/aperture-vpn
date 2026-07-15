# Best-server selection

ICMP ping needs raw sockets (`CAP_NET_RAW`) that unrooted Android apps do
not have, and `InetAddress.isReachable()` silently falls back and fails.
So the app measures TCP connect time to each profile endpoint and blends it
with VPN Gate metadata.

## What to probe

Each decoded `.ovpn` has a `remote <host> <port>` plus `proto` directive.
Extract `(ip, port, proto)`. TCP connect timing is only meaningful on TCP
endpoints, so:

- Prefer TCP profiles for probeability (VPN Gate exposes many TCP:443
  endpoints; they are also NAT-friendly).
- For UDP-only profiles, fall back to metadata-only scoring, or probe TCP
  reachability of the same IP as a rough liveness signal.

## Probe mechanics

- `Socket.connect(InetSocketAddress(ip, port), timeoutMs)` with a
  1500-2000 ms timeout, measure the `System.nanoTime()` delta on success,
  close immediately. Record success/failure plus latency.
- Concurrency: Kotlin coroutines on `Dispatchers.IO` with a
  `Semaphore(64)` cap so 100+ probes finish in a few seconds without
  exhausting file descriptors:
  `endpoints.map { async { semaphore.withPermit { probe(it) } } }.awaitAll()`
  wrapped in a global `withTimeout`.
- Cache probe results with a short TTL so re-selection is instant; probe on
  user action or unmetered networks by default to save data.

## Scoring formula

Hard-filter first: drop unreachable endpoints, then normalize each component
to [0,1] and weight. Higher is better:

```
norm_latency   = 1 - clamp(connectMs / 2000, 0, 1)      # live TCP connect
norm_speed     = clamp(speedBps / 100_000_000, 0, 1)     # VPN Gate Speed
norm_score     = clamp(score / batchMaxScore, 0, 1)      # VPN Gate Score
norm_load      = 1 - clamp(numVpnSessions / 200, 0, 1)   # fewer users better
norm_meta_ping = 1 - clamp(metaPingMs / 1000, 0, 1)      # VPN Gate Ping

server_score = 0.40 * norm_latency
             + 0.25 * norm_speed
             + 0.15 * norm_score
             + 0.10 * norm_load
             + 0.10 * norm_meta_ping
```

Live TCP latency dominates because it reflects the user's actual network
right now; VPN Gate's `Ping` and `Score` are measured from VPN Gate's
vantage point and are secondary signals.

## Smart connect loop

TCP connect success does not equal a working tunnel. Keep the top N as a
ranked fallback list: connect to `argmax(server_score)`, verify the tunnel
with a quick HTTP HEAD through it (with timeout), and on failure
automatically retry the next-ranked server. Optional user filters: country
picker and a `LogType` preference.

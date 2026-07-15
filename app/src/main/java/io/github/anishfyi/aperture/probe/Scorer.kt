package io.github.anishfyi.aperture.probe

import io.github.anishfyi.aperture.data.ProbeResult
import io.github.anishfyi.aperture.data.RankedServer
import io.github.anishfyi.aperture.data.ServerProfile

object Scorer {
    private const val WEIGHT_LATENCY = 0.40
    private const val WEIGHT_SPEED = 0.25
    private const val WEIGHT_SCORE = 0.15
    private const val WEIGHT_LOAD = 0.10
    private const val WEIGHT_META_PING = 0.10

    fun rank(
        servers: List<ServerProfile>,
        probes: Map<String, ProbeResult>,
    ): List<RankedServer> {
        val reachable = servers.mapNotNull { server ->
            val probe = probes[server.ip] ?: ProbeResult(false, null)
            if (!probe.success && server.proto.equals("tcp", ignoreCase = true)) {
                null
            } else {
                server to probe
            }
        }
        if (reachable.isEmpty()) {
            return emptyList()
        }

        val batchMaxScore = reachable.maxOf { it.first.score }.coerceAtLeast(1L)

        return reachable
            .map { (server, probe) ->
                RankedServer(
                    profile = server,
                    probe = probe,
                    score = computeScore(server, probe, batchMaxScore),
                )
            }
            .sortedByDescending { it.score }
    }

    private fun computeScore(
        server: ServerProfile,
        probe: ProbeResult,
        batchMaxScore: Long,
    ): Double {
        val connectMs = probe.latencyMs ?: 2000L
        val normLatency = 1.0 - clamp(connectMs / 2000.0, 0.0, 1.0)
        val normSpeed = clamp(server.speedBps / 100_000_000.0, 0.0, 1.0)
        val normScore = clamp(server.score / batchMaxScore.toDouble(), 0.0, 1.0)
        val normLoad = 1.0 - clamp(server.numVpnSessions / 200.0, 0.0, 1.0)
        val metaPing = server.pingMs ?: 1000L
        val normMetaPing = 1.0 - clamp(metaPing / 1000.0, 0.0, 1.0)

        return WEIGHT_LATENCY * normLatency +
            WEIGHT_SPEED * normSpeed +
            WEIGHT_SCORE * normScore +
            WEIGHT_LOAD * normLoad +
            WEIGHT_META_PING * normMetaPing
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }
}

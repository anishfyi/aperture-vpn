package io.github.anishfyi.aperture.probe

import io.github.anishfyi.aperture.data.ProbeResult
import io.github.anishfyi.aperture.data.ServerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class Prober(
    private val timeoutMs: Int = 2000,
    private val maxConcurrency: Int = 64,
) {
    suspend fun probeAll(servers: List<ServerProfile>): Map<String, ProbeResult> = withContext(Dispatchers.IO) {
        val semaphore = Semaphore(maxConcurrency)
        servers.map { server ->
            async {
                semaphore.withPermit {
                    server.ip to probe(server)
                }
            }
        }.awaitAll().toMap()
    }

    fun probe(server: ServerProfile): ProbeResult {
        if (server.proto.equals("udp", ignoreCase = true)) {
            return probeTcpFallback(server)
        }
        return tcpProbe(server.remoteHost, server.remotePort)
    }

    private fun probeTcpFallback(server: ServerProfile): ProbeResult {
        val tcpPort = if (server.remotePort == 1194) 443 else server.remotePort
        return tcpProbe(server.remoteHost, tcpPort)
    }

    private fun tcpProbe(host: String, port: Int): ProbeResult {
        val start = System.nanoTime()
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                val elapsedMs = (System.nanoTime() - start) / 1_000_000
                ProbeResult(success = true, latencyMs = elapsedMs)
            }
        } catch (_: Exception) {
            ProbeResult(success = false, latencyMs = null)
        }
    }
}

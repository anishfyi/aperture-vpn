package io.github.anishfyi.aperture.data

data class ServerProfile(
    val ip: String,
    val hostName: String,
    val score: Long,
    val pingMs: Long?,
    val speedBps: Long,
    val countryLong: String,
    val countryShort: String,
    val numVpnSessions: Int,
    val logType: String,
    val operator: String,
    val message: String,
    val ovpnConfig: String,
    val remoteHost: String,
    val remotePort: Int,
    val proto: String,
    val lastSeenMs: Long = System.currentTimeMillis(),
)

data class ProfileCache(
    val lastFetchMs: Long = 0L,
    val nextFetchAfterMs: Long = 0L,
    val servers: Map<String, ServerProfile> = emptyMap(),
)

data class ProbeResult(
    val success: Boolean,
    val latencyMs: Long?,
)

data class RankedServer(
    val profile: ServerProfile,
    val probe: ProbeResult,
    val score: Double,
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR,
}

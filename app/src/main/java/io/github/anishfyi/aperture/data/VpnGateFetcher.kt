package io.github.anishfyi.aperture.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class VpnGateFetcher(
    context: Context,
    private val cacheStore: ProfileCacheStore = ProfileCacheStore(context),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build(),
) {
    private var failureBackoffMs = MIN_BACKOFF_MS

    suspend fun getProfiles(forceRefresh: Boolean = false): List<ServerProfile> {
        val cache = cacheStore.load()
        val now = System.currentTimeMillis()
        if (!forceRefresh && cache.servers.isNotEmpty() && now < cache.nextFetchAfterMs) {
            return cache.servers.values.toList()
        }

        return withContext(Dispatchers.IO) {
            val refreshed = runCatching { fetchAndMerge(cache) }
            if (refreshed.isSuccess) {
                failureBackoffMs = MIN_BACKOFF_MS
                refreshed.getOrThrow()
            } else {
                val backoffUntil = now + failureBackoffMs
                failureBackoffMs = (failureBackoffMs * 2).coerceAtMost(MAX_BACKOFF_MS)
                if (cache.servers.isNotEmpty()) {
                    cacheStore.save(
                        cache.copy(nextFetchAfterMs = backoffUntil),
                    )
                    cache.servers.values.toList()
                } else {
                    delay(failureBackoffMs)
                    throw refreshed.exceptionOrNull() ?: IllegalStateException("fetch failed")
                }
            }
        }
    }

    private fun fetchAndMerge(existing: ProfileCache): List<ServerProfile> {
        val request = Request.Builder()
            .url(API_URL)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("VPN Gate HTTP ${response.code}")
            }
            response.body?.string() ?: error("empty response")
        }

        val fresh = parseCsv(body)
        val merged = existing.servers.toMutableMap()
        val now = System.currentTimeMillis()
        fresh.forEach { profile ->
            merged[profile.ip] = profile.copy(lastSeenMs = now)
        }

        val ttlMs = Random.nextLong(MIN_TTL_MS, MAX_TTL_MS + 1)
        val updated = ProfileCache(
            lastFetchMs = now,
            nextFetchAfterMs = now + ttlMs,
            servers = merged,
        )
        cacheStore.save(updated)
        return merged.values.toList()
    }

    internal fun parseCsv(body: String): List<ServerProfile> {
        val profiles = mutableListOf<ServerProfile>()
        body.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("*")) {
                return@forEach
            }

            val dataLine = if (line.startsWith("#")) line.removePrefix("#") else line
            val fields = dataLine.split(",", limit = 15)
            if (fields.size != FIELD_COUNT) {
                return@forEach
            }

            val base64 = fields[14].trim()
            if (base64.isEmpty()) {
                return@forEach
            }

            val prepared = OvpnParser.decodeAndPrepare(base64) ?: return@forEach
            val pingMs = fields[3].trim().takeIf { it.isNotEmpty() && it != "-" }?.toLongOrNull()

            profiles += ServerProfile(
                ip = fields[1].trim(),
                hostName = fields[0].trim(),
                score = fields[2].trim().toLongOrNull() ?: 0L,
                pingMs = pingMs,
                speedBps = fields[4].trim().toLongOrNull() ?: 0L,
                countryLong = fields[5].trim(),
                countryShort = fields[6].trim(),
                numVpnSessions = fields[7].trim().toIntOrNull() ?: 0,
                logType = fields[11].trim(),
                operator = fields[12].trim(),
                message = fields[13].trim(),
                ovpnConfig = prepared.ovpnConfig,
                remoteHost = prepared.remoteHost,
                remotePort = prepared.remotePort,
                proto = prepared.proto,
            )
        }
        return profiles
    }

    companion object {
        const val API_URL = "https://www.vpngate.net/api/iphone/"
        const val USER_AGENT = "ApertureVPN/1.0 (+https://github.com/anishfyi/aperture-vpn)"
        private const val FIELD_COUNT = 15
        private const val MIN_TTL_MS = 6L * 60 * 60 * 1000
        private const val MAX_TTL_MS = 24L * 60 * 60 * 1000
        private const val MIN_BACKOFF_MS = 30_000L
        private const val MAX_BACKOFF_MS = 30L * 60 * 1000
    }
}

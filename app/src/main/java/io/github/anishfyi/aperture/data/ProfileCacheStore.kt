package io.github.anishfyi.aperture.data

import android.content.Context
import org.json.JSONObject
import java.io.File

class ProfileCacheStore(context: Context) {
    private val cacheFile = File(context.filesDir, CACHE_FILE)

    fun load(): ProfileCache {
        if (!cacheFile.exists()) {
            return ProfileCache()
        }
        return runCatching {
            parseCache(JSONObject(cacheFile.readText()))
        }.getOrDefault(ProfileCache())
    }

    fun save(cache: ProfileCache) {
        cacheFile.writeText(toJson(cache).toString())
    }

    private fun toJson(cache: ProfileCache): JSONObject {
        val serversJson = JSONObject()
        cache.servers.forEach { (ip, profile) ->
            serversJson.put(ip, profileToJson(profile))
        }
        return JSONObject()
            .put("lastFetchMs", cache.lastFetchMs)
            .put("nextFetchAfterMs", cache.nextFetchAfterMs)
            .put("servers", serversJson)
    }

    private fun parseCache(root: JSONObject): ProfileCache {
        val serversObj = root.optJSONObject("servers") ?: JSONObject()
        val servers = buildMap {
            serversObj.keys().forEach { ip ->
                val profileObj = serversObj.optJSONObject(ip) ?: return@forEach
                put(ip, jsonToProfile(profileObj))
            }
        }
        return ProfileCache(
            lastFetchMs = root.optLong("lastFetchMs"),
            nextFetchAfterMs = root.optLong("nextFetchAfterMs"),
            servers = servers,
        )
    }

    private fun profileToJson(profile: ServerProfile): JSONObject {
        return JSONObject()
            .put("ip", profile.ip)
            .put("hostName", profile.hostName)
            .put("score", profile.score)
            .put("pingMs", profile.pingMs ?: JSONObject.NULL)
            .put("speedBps", profile.speedBps)
            .put("countryLong", profile.countryLong)
            .put("countryShort", profile.countryShort)
            .put("numVpnSessions", profile.numVpnSessions)
            .put("logType", profile.logType)
            .put("operator", profile.operator)
            .put("message", profile.message)
            .put("ovpnConfig", profile.ovpnConfig)
            .put("remoteHost", profile.remoteHost)
            .put("remotePort", profile.remotePort)
            .put("proto", profile.proto)
            .put("lastSeenMs", profile.lastSeenMs)
    }

    private fun jsonToProfile(obj: JSONObject): ServerProfile {
        val pingValue = if (obj.isNull("pingMs")) null else obj.optLong("pingMs")
        return ServerProfile(
            ip = obj.getString("ip"),
            hostName = obj.optString("hostName"),
            score = obj.optLong("score"),
            pingMs = pingValue,
            speedBps = obj.optLong("speedBps"),
            countryLong = obj.optString("countryLong"),
            countryShort = obj.optString("countryShort"),
            numVpnSessions = obj.optInt("numVpnSessions"),
            logType = obj.optString("logType"),
            operator = obj.optString("operator"),
            message = obj.optString("message"),
            ovpnConfig = obj.getString("ovpnConfig"),
            remoteHost = obj.optString("remoteHost"),
            remotePort = obj.optInt("remotePort"),
            proto = obj.optString("proto"),
            lastSeenMs = obj.optLong("lastSeenMs", System.currentTimeMillis()),
        )
    }

    companion object {
        private const val CACHE_FILE = "vpngate_profiles.json"
    }
}

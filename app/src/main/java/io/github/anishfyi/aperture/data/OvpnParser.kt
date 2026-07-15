package io.github.anishfyi.aperture.data

object OvpnParser {
    private val remoteRegex = Regex("""^remote\s+(\S+)\s+(\d+)\s*$""", RegexOption.MULTILINE)
    private val protoRegex = Regex("""^proto\s+(tcp|udp)\s*$""", RegexOption.IGNORE_CASE or RegexOption.MULTILINE)

    const val DNS1 = "94.140.14.14"
    const val DNS2 = "94.140.15.15"

    fun decodeAndPrepare(base64Config: String): PreparedConfig? {
        val decoded = runCatching {
            String(android.util.Base64.decode(base64Config.trim(), android.util.Base64.DEFAULT), Charsets.UTF_8)
        }.getOrNull() ?: return null

        val withDns = injectDns(decoded)
        val endpoint = extractEndpoint(withDns) ?: return null
        return PreparedConfig(
            ovpnConfig = withDns,
            remoteHost = endpoint.host,
            remotePort = endpoint.port,
            proto = endpoint.proto,
        )
    }

    fun injectDns(config: String): String {
        val trimmed = config.trimEnd()
        val dnsBlock = buildString {
            appendLine("dhcp-option DNS $DNS1")
            appendLine("dhcp-option DNS $DNS2")
            appendLine("pull-filter ignore \"dhcp-option DNS\"")
        }
        return "$trimmed\n$dnsBlock"
    }

    fun extractEndpoint(config: String): Endpoint? {
        val remoteMatch = remoteRegex.find(config) ?: return null
        val host = remoteMatch.groupValues[1]
        val port = remoteMatch.groupValues[2].toIntOrNull() ?: return null
        val proto = protoRegex.find(config)?.groupValues?.get(1)?.lowercase() ?: "tcp"
        return Endpoint(host = host, port = port, proto = proto)
    }

    data class PreparedConfig(
        val ovpnConfig: String,
        val remoteHost: String,
        val remotePort: Int,
        val proto: String,
    )

    data class Endpoint(
        val host: String,
        val port: Int,
        val proto: String,
    )
}

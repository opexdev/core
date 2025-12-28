package co.nilin.opex.api.app.security

interface ApiKeyRegistry {
    data class BotInfo(
        val apiKeyId: String,
        val hmacSecret: String,
        val enabled: Boolean = true,
        val allowedIps: Set<String>? = null,
        val keycloakUsername: String? = null
    )

    fun find(apiKeyId: String): BotInfo?
}

package co.nilin.opex.wallet.core.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

object JwtUtils {
    private val mapper = jacksonObjectMapper()

    fun decodePayload(token: String): Map<String, Any> {
        val parts = token.split(".")
        require(parts.size == 3) { "Invalid JWT token" }
        val json = String(Base64.getUrlDecoder().decode(parts[1]))
        return mapper.readValue(json, Map::class.java) as Map<String, Any>
    }

    fun extractRoles(token: String): List<String> {
        val payload = decodePayload(token)
        return (payload["roles"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }
}
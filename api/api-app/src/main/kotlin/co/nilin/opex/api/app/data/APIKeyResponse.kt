package co.nilin.opex.api.app.data

import java.time.LocalDateTime

data class ApiKeyResponse(
    val apiKeyId: String,
    val label: String?,
    val enabled: Boolean,
    val allowedIps: Set<String>?,
    val allowedEndpoints: Set<String>?,
    val keycloakUsername: String?,
    val secret: String? = null, // only present on create/rotate
)
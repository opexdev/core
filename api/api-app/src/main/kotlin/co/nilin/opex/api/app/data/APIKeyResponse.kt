package co.nilin.opex.api.app.data

import java.time.LocalDateTime
import java.util.*

data class APIKeyResponse(
    val label: String,
    val expirationTime: LocalDateTime,
    val allowedIPs: String?,
    val key: String,
    val enabled: Boolean
)
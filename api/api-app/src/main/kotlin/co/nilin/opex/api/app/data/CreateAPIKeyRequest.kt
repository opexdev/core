package co.nilin.opex.api.app.data

import java.time.LocalDateTime

data class CreateAPIKeyRequest(
    val label: String,
    val expirationTime: LocalDateTime,
    val allowedIPs: String
)
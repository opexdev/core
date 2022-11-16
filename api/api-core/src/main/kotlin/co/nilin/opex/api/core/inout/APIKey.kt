package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class APIKey(
    val userId: String,
    val label: String,
    val accessToken: String?,
    val expirationTime: LocalDateTime?,
    val allowedIPs: String?,
    val key: String,
    val isEnabled: Boolean,
    val isExpired: Boolean
)
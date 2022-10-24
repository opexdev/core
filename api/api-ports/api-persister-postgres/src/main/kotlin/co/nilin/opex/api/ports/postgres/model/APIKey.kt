package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("api_key")
data class APIKey(
    val id: Long? = null,
    val userId: String,
    val label: String,
    val accessToken: String,
    val refreshToken: String,
    val expirationTime: LocalDateTime,
    val allowedIPs: String?,
    val key: String = UUID.randomUUID().toString(),
    var isEnabled: Boolean = true
)
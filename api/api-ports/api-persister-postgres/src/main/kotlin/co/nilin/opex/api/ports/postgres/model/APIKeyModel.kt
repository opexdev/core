package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("api_key")
data class APIKeyModel(
    @Id val id: Long? = null,
    val userId: String,
    val label: String,
    var accessToken: String,
    var refreshToken: String,
    val expirationTime: LocalDateTime?,
    @Column("allowed_ips")
    val allowedIPs: String?,
    var tokenExpirationTime: LocalDateTime,
    val key: String = UUID.randomUUID().toString(),
    var isEnabled: Boolean = true,
    var isExpired: Boolean = true
)
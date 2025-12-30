package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "rate_limit_group")
data class RateLimitGroupModel(
    @Id
    val id: Long? = null,
    val name: String,
    val requestCount: Int,
    val requestWindowSeconds: Int,
    val cooldownSeconds: Int,
    val maxPenaltyLevel: Int,
    val enabled: Boolean = true
)

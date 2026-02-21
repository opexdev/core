package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "rate_limit_penalty")
data class RateLimitPenaltyModel(
    @Id
    val id: Long? = null,
    val groupId: Long,
    val blockStep: Int,
    val blockDurationSeconds: Int
)

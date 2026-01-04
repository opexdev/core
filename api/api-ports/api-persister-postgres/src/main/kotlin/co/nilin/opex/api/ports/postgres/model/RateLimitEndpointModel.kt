package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "rate_limit_endpoint")
data class RateLimitEndpointModel(
    @Id
    val id: Long? = null,
    val url: String,
    val method: String,
    val groupId: Long,
    val priority: Int,
    val enabled: Boolean = true
)
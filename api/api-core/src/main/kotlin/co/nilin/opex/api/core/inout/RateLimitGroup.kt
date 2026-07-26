package co.nilin.opex.api.core.inout

data class RateLimitGroup(
    val id: Long? = null,
    val name: String,
    val requestCount: Int,
    val requestWindowSeconds: Int,
    val cooldownSeconds: Int,
    val maxPenaltyLevel: Int,
    val enabled: Boolean = true
)
